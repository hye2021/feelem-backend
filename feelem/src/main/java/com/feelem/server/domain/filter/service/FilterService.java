package com.feelem.server.domain.filter.service;

import com.feelem.server.domain.filter.dto.FilterCreateRequest;
import com.feelem.server.domain.filter.dto.FilterCreateRequest.FaceSticker;
import com.feelem.server.domain.filter.dto.FilterListResponse;
import com.feelem.server.domain.filter.dto.FilterResponse;
import com.feelem.server.domain.filter.dto.FilterResponse.FaceStickerResponse;
import com.feelem.server.domain.filter.dto.FilterSortType;
import com.feelem.server.domain.filter.dto.PriceDisplayType;
import com.feelem.server.domain.filter.dto.SearchType;
import com.feelem.server.domain.filter.entity.Bookmark;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.entity.FilterTag;
import com.feelem.server.domain.filter.entity.Tag;
import com.feelem.server.domain.filter.repository.BookmarkRepository;
import com.feelem.server.domain.filter.repository.FilterRepository;
import com.feelem.server.domain.filter.repository.FilterTagRepository;
import com.feelem.server.domain.filter.repository.TagRepository;
import com.feelem.server.domain.finance.entity.FilterTransaction;
import com.feelem.server.domain.finance.entity.FilterTransactionType;
import com.feelem.server.domain.finance.repository.FilterTransactionRepository;
import com.feelem.server.domain.sticker.entity.FaceStickerPlacement;
import com.feelem.server.domain.sticker.entity.Sticker;
import com.feelem.server.domain.sticker.repository.FaceStickerPlacementRepository;
import com.feelem.server.domain.sticker.repository.StickerRepository;
import com.feelem.server.domain.user.entity.Point;
import com.feelem.server.domain.user.entity.Social;
import com.feelem.server.domain.user.entity.SocialType;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.PointRepository;
import com.feelem.server.domain.user.service.UserService;
import com.feelem.server.recommend.FilterRecommendMapper;
import com.feelem.server.recommend.RecommendServingClient;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class FilterService {

  private final UserService userService;
  private final FilterRepository filterRepository;
  private final StickerRepository stickerRepository;
  private final TagRepository tagRepository;
  private final FilterTagRepository filterTagRepository;
  private final BookmarkRepository bookmarkRepository;
  private final FilterTransactionRepository filterTransactionRepository; // 이름 통일 (Repo 제거)
  private final PointRepository pointRepository;
  private final FaceStickerPlacementRepository faceStickerPlacementRepository;
  private final ApplicationEventPublisher eventPublisher;

  // AI recommend 서버 연동을 위한 의존성
  private final FilterRecommendMapper aiMapper;
  private final RecommendServingClient aiClient;

  /**
   * 필터 생성
   */
  public Filter createFilter(FilterCreateRequest request) {
    log.info("🚀 [FilterService] createFilter 호출됨");

    // 1. 스티커 리스트 상태 로그
    if (request.getStickers() == null) {
      log.error("🚨 [문제발견] request.getStickers()가 NULL입니다!");
    } else {
      log.info("🔍 [데이터확인] request.getStickers() 크기: {}개", request.getStickers().size());
      if (request.getStickers().isEmpty()) {
        log.warn("⚠️ [데이터확인] 리스트는 존재하지만 비어있습니다 (Size=0).");
      }
    }

    // 제작자
    User creator = userService.getCurrentUser();
    // SNS 종류
    SocialType type = SocialType.fromString(
        request.getSocialType() != null ? request.getSocialType() : "NONE"
    );
    // User의 Social 연결
    Social social = userService.getSocialByUser(creator);

    // 필터 엔티티 생성
    Filter filter = Filter.builder()
        .name(request.getName())
        .creator(creator)
        .price(request.getPrice())
        .originalImageUrl(request.getOriginalImageUrl())
        .editedImageUrl(request.getEditedImageUrl())
        .stickerImageNoFaceUrl(request.getStickerImageNoFaceUrl())
        .aspectX(request.getAspectX())
        .aspectY(request.getAspectY())
        .colorAdjustments(request.getColorAdjustments())
        .socialType(type)
        .social(social)
        .build();
    Filter savedFilter = filterRepository.save(filter);
    log.info("✔️ 필터 기본 정보 저장 완료. ID: {}", savedFilter.getId());

    // 태그 등록
    List<String> tagNames = request.getTags();
    if (tagNames != null && !tagNames.isEmpty()) {
      for (String tagName : tagNames) {
        Tag tag = tagRepository.findByName(tagName)
            .orElseGet(() -> tagRepository.save(new Tag(tagName)));
        filterTagRepository.save(new FilterTag(savedFilter, tag));
      }
    }

    // 스티커 배치 등록
    List<FaceStickerPlacement> savedStickers = new ArrayList<>();

    if (request.getStickers() != null && !request.getStickers().isEmpty()) {
      log.info("🔄 스티커 저장 반복문 진입 (총 {}개)", request.getStickers().size());

      for (FaceSticker placement : request.getStickers()) {
        try {
          Sticker sticker = stickerRepository.findById(placement.getStickerId())
              .orElseThrow(() -> new EntityNotFoundException("Sticker not found: " + placement.getStickerId()));

          FaceStickerPlacement facePlacement = new FaceStickerPlacement(
              savedFilter,
              sticker,
              placement.getRelX(),
              placement.getRelY(),
              placement.getRelW(),
              placement.getRelH(),
              placement.getRot()
          );

          log.info("   💾 스티커 저장 시도 - Sticker ID: {}, relX: {}", placement.getStickerId(), placement.getRelX());

          faceStickerPlacementRepository.save(facePlacement);
          savedStickers.add(facePlacement);

        } catch (Exception e) {
          log.error("❌ 스티커 저장 중 에러 발생 (ID: {}): {}", placement.getStickerId(), e.getMessage());
          throw e;
        }
      }
    } else {
      log.info("⏭️ 저장할 스티커가 없어서 반복문을 건너뜁니다.");
    }

    log.info("✅ 필터 생성 최종 완료. 저장된 스티커 수: {}", savedStickers.size());

    // AI 추천 서버에 인덱싱 요청
    try {
      aiClient.indexFilter(aiMapper.toIndexRequest(savedFilter));
      log.info("🤖 AI Server Indexing Requested for Filter ID: {}", savedFilter.getId());
    } catch (Exception e) {
      log.warn("⚠️ AI Indexing Failed: {}", e.getMessage());
    }

    return savedFilter;
  }

  /**
   * 필터 상세 조회
   */
  @Transactional(readOnly = true)
  public FilterResponse getFilter(Long filterId) {
    Filter filter = filterRepository.findByIdAndIsDeletedFalse(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));

    List<String> tags = filter.getFilterTags().stream()
        .map(ft -> ft.getTag().getName())
        .collect(Collectors.toList());

    List<FaceStickerResponse> stickers = filter.getFaceStickerPlacements().stream()
        .map(fp -> new FaceStickerResponse(
            fp.getSticker().getId(),
            fp.getSticker().getImageUrl(),
            fp.getRelX(),
            fp.getRelY(),
            fp.getRelW(),
            fp.getRelH(),
            fp.getRot()
        ))
        .collect(Collectors.toList());

    // 현재 유저와 비교해서, 이 필터 제작자가 나인지 확인
    User currentUser = userService.getCurrentUser();
    boolean isMine = filter.getCreator().getId().equals(currentUser.getId());
    // 현재 사용자가 구매 혹은 사용한 필터인지 확인
    boolean isUsed = filterTransactionRepository.existsByBuyerIdAndFilterId(currentUser.getId(), filterId);
    // 현재 사용자가 북마크한 필터인지 확인
    boolean isBookmarked = bookmarkRepository.existsByUserAndFilter(currentUser, filter);

    return new FilterResponse(filter, isMine, isUsed, isBookmarked, tags, stickers);
  }

  /**
   * 필터 엔티티 조회 헬퍼
   */
  @Transactional(readOnly = true)
  public Filter findById(Long filterId) {
    return filterRepository.findByIdAndIsDeletedFalse(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));
  }

  /**
   * 헬퍼 메소드: ID 리스트 순서대로 객체 정렬
   */
  private List<Filter> sortByIdListOrder(List<Filter> filters, List<String> targetIdOrder) {
    var filterMap = filters.stream()
        .collect(Collectors.toMap(f -> String.valueOf(f.getId()), Function.identity()));

    return targetIdOrder.stream()
        .filter(filterMap::containsKey)
        .map(filterMap::get)
        .collect(Collectors.toList());
  }

  /**
   * 추천 기반 검색 및 정렬
   */
  @Transactional(readOnly = true)
  public List<FilterListResponse> searchFilters(
      String query,
      SearchType searchType,
      FilterSortType sortType,
      Pageable pageable
  ) {
    User user = userService.getCurrentUser();

    // 최종 결과를 담을 리스트
    List<Filter> filters = new ArrayList<>();

    // =================================================================
    // 1. 검색 타입에 따른 데이터 확보
    // =================================================================
    if (searchType == SearchType.TAG) {
      // [A] 태그 검색
      List<String> tags = List.of(query.split(","));
      filters = filterRepository.findByTagsContainingAll(tags, (long) tags.size());

    } else {
      // [B] 자연어(하이브리드) 검색: 제목 검색 + AI 검색

      // 1) DB 제목 검색 (정확도 높음 -> 우선 순위)
      List<Filter> nameMatches = filterRepository.findByNameContainingAndIsDeletedFalseOrderByCreatedAtDesc(query);

      // 2) AI 의미 검색 (연관성 높음 -> 후순위)
      //    AI에게는 페이징 없이 상위 100~200개 정도를 한 번에 달라고 요청하는 것이 좋습니다.
      //    (합쳐서 페이징을 다시 해야 하기 때문)
      List<String> aiSearchIds = aiClient.getSearchResults(query, 0); // 0페이지(상위 결과)만 요청

      List<Filter> aiMatches = new ArrayList<>();
      if (!aiSearchIds.isEmpty()) {
        List<Long> ids = aiSearchIds.stream().map(Long::parseLong).toList();
        List<Filter> foundFilters = filterRepository.findAllById(ids);
        // AI가 추천해준 순서(유사도 순)대로 정렬
        aiMatches = sortByIdListOrder(foundFilters, aiSearchIds);
      }

      // 3) 병합 (Merge) & 중복 제거
      //    LinkedHashSet을 사용해 순서 보장 (제목 검색 결과 먼저 -> 그 뒤에 AI 결과)
      Set<Filter> mergedSet = new LinkedHashSet<>();
      mergedSet.addAll(nameMatches); // 제목 일치 필터 먼저 넣기
      mergedSet.addAll(aiMatches);   // AI 추천 필터 뒤에 넣기 (이미 있는 건 중복 제거됨)

      filters = new ArrayList<>(mergedSet);
    }

    if (filters.isEmpty()) {
      return Collections.emptyList();
    }

    // =================================================================
    // 2. 정렬 로직 (메모리 정렬)
    // =================================================================
    Stream<Filter> stream = filters.stream();

    switch (sortType) {
      case ACCURACY -> {
        // 자연어 검색일 경우: 이미 위에서 [제목 일치 -> AI 랭킹] 순서로 합쳤으므로
        // 별도의 정렬을 하지 않는 것이 '정확도 순'입니다.
        // 단, 태그 검색일 경우엔 최신순으로 Fallback
        if (searchType == SearchType.TAG) {
          stream = stream.sorted(Comparator.comparing(Filter::getCreatedAt).reversed());
        }
      }
      case POPULARITY -> // 저장(Save) 수 기준 내림차순
          stream = stream.sorted(Comparator.comparing(Filter::getSaveCount).reversed());

      case LATEST -> // 최신 등록순
          stream = stream.sorted(Comparator.comparing(Filter::getCreatedAt).reversed());

      case LOW_PRICE -> // 낮은 가격순
          stream = stream.sorted(Comparator.comparing(Filter::getPrice));

      case REVIEW_COUNT -> // 사용 수 내림차순
          stream = stream.sorted(Comparator.comparing(Filter::getUseCount).reversed());
    }

    List<Filter> sortedFilters = stream.collect(Collectors.toList());

    // =================================================================
    // 3. 페이징 처리 (수동)
    // =================================================================
    // 두 리스트를 합쳤기 때문에 전체 개수가 달라졌습니다.
    // 따라서 AI 페이징 대신, 합쳐진 리스트를 기준으로 자바에서 잘라줘야 합니다.

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), sortedFilters.size());

    // 요청한 페이지가 전체 개수를 넘어가면 빈 리스트 반환
    if (start >= sortedFilters.size()) {
      return Collections.emptyList();
    }

    // 해당 페이지 부분만 자르기 (SubList)
    List<Filter> pagedFilters = sortedFilters.subList(start, end);

    // 4. DTO 변환
    return pagedFilters.stream()
        .map(f -> toFilterListResponse(f, user))
        .collect(Collectors.toList());
  }

  /**
   * 홈 화면 용 - 추천 필터 페이징 조회
   */
  @Transactional(readOnly = true)
  public List<FilterListResponse> getHomeRecommendations(Pageable pageable) {
    User user = userService.getCurrentUser();
    int page = pageable.getPageNumber();

    // 1. 유저의 선호 필터 ID 조회
    List<Long> recentIds = bookmarkRepository.findRecentBookmarkedFilterIds(
        user.getId(),
        PageRequest.of(0, 10)
    );

    List<String> likedFilterIds = recentIds.stream()
        .map(String::valueOf)
        .collect(Collectors.toList());

    // 2. AI 서버 요청
    List<String> recommendedIds = aiClient.getHomeRecommendations(likedFilterIds, page);

    // 3. 결과가 없으면 최신순 Fallback
    if (recommendedIds.isEmpty()) {
      log.info("⚠️ AI 추천 결과 없음 -> 최신순 Fallback");
      return getRecentFilters(pageable).getContent();
    }

    // 4. DB 조회
    List<Long> ids = recommendedIds.stream().map(Long::parseLong).toList();
    List<Filter> filters = filterRepository.findAllById(ids);

    // 5. 정렬 및 DTO 변환
    return sortByIdListOrder(filters, recommendedIds).stream()
        .map(f -> toFilterListResponse(f, user))
        .collect(Collectors.toList());
  }

  /**
   * 홈 화면용 - 최신 등록 필터 페이징 조회
   */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getRecentFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = filterRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc(pageable);
    log.info("➡️ 홈화면 - 최신 필터 조회");
    return page.map(filter -> toFilterListResponse(filter, user));
  }

  /**
   * 홈 화면용 - 인기 등록 필터 페이징 조회
   */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getHotFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = filterRepository.findAllByIsDeletedFalseOrderBySaveCountDesc(pageable);
    log.info("➡️ 홈화면 - 인기 필터 조회");
    return page.map(filter -> toFilterListResponse(filter, user));
  }

  /**
   * 홈 화면용 - 완전 랜덤 필터 페이징 조회
   */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getRandomFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = filterRepository.findAllByIsDeletedFalseOrderByRandom(pageable);
    log.info("➡️ 홈화면 - 랜덤 필터 조회");
    return page.map(filter -> toFilterListResponse(filter, user));
  }

  /**
   * 아카이브 - 내가 제작한 필터 목록 조회
   */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getMyFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = filterRepository.findAllByCreatorIdAndIsDeletedFalseOrderByCreatedAtDesc(
        user.getId(), pageable);
    return page.map(filter -> toFilterListResponse(filter, user));
  }

  /**
   * 아카이브 - 내가 사용 & 구매한 필터 목록 조회
   */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getUsedFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = filterTransactionRepository.findUsedOrPurchasedFilters(user.getId(), pageable);
    return page.map(filter -> {
      boolean bookmark = bookmarkRepository.existsByUserAndFilter(user, filter);
      // 이미 구매/사용한 목록이므로 usage=true
      PriceDisplayType type = PriceDisplayType.getType(true, filter.getPrice());
      return FilterListResponse.from(filter, type, bookmark);
    });
  }

  /**
   * 북마크 토글 (동시성 처리 적용)
   * @return 갱신된 최신 북마크 수 (saveCount)
   */
  @Transactional
  public void toggleBookmark(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId); // 1. 초기 조회

    boolean exists = bookmarkRepository.existsByUserAndFilter(user, filter);

    if (exists) {
      bookmarkRepository.deleteByUserAndFilter(user, filter);
      // 2. 카운트 감소 (DB 직접 업데이트 & 1차 캐시 초기화됨)
      filterRepository.decreaseSaveCount(filter.getId());
    } else {
      bookmarkRepository.save(new Bookmark(user, filter));
      // 2. 카운트 증가 (DB 직접 업데이트 & 1차 캐시 초기화됨)
      filterRepository.increaseSaveCount(filter.getId());
    }

    // 만약 필터 정보를 다시 조회해야 한다면
    // 동시성 이슈로 인해 반영이 지연될 수 있으므로, 갱신된 필터 엔티티를 다시 조회
//    Filter updatedFilter = filterRepository.findByIdAndIsDeletedFalse(filterId)
//        .orElseThrow(() -> new EntityNotFoundException("Filter not found after update"));
  }

  /**
   * 북마크 여부 확인
   */
  @Transactional(readOnly = true)
  public boolean isBookmarked(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);
    return bookmarkRepository.existsByUserAndFilter(user, filter);
  }

  /**
   * 북마크 추가 (동시성 처리 적용)
   */
  @Transactional
  public void addBookmark(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);

    if (bookmarkRepository.existsByUserAndFilter(user, filter)) {
      throw new IllegalStateException("Already bookmarked");
    }

    bookmarkRepository.save(new Bookmark(user, filter));
    // Repository 쿼리 호출
    filterRepository.increaseSaveCount(filterId);
  }

  /**
   * 북마크 제거 (동시성 처리 적용)
   */
  @Transactional
  public void removeBookmark(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);

    Bookmark bookmark = bookmarkRepository.findByUserAndFilter(user, filter)
        .orElseThrow(() -> new EntityNotFoundException("Bookmark not found"));

    bookmarkRepository.delete(bookmark);
    // Repository 쿼리 호출
    filterRepository.decreaseSaveCount(filterId);
  }

  /**
   * 북마크한 필터 목록 조회
   */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getBookmarkedFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = bookmarkRepository.findBookmarkedFilters(user.getId(), pageable);

    return page.map(filter -> {
      boolean usage = filterTransactionRepository.existsByBuyerIdAndFilterId(user.getId(),
          filter.getId());
      PriceDisplayType type = PriceDisplayType.getType(usage, filter.getPrice());
      return FilterListResponse.from(filter, type, true);
    });
  }

  /**
   * Filter -> FilterListResponse 변환 헬퍼
   */
  private FilterListResponse toFilterListResponse(Filter filter, User user) {
    boolean bookmark = bookmarkRepository.existsByUserAndFilter(user, filter);
    boolean usage = filterTransactionRepository.existsByBuyerIdAndFilterId(user.getId(), filter.getId());
    PriceDisplayType type = PriceDisplayType.getType(usage, filter.getPrice());

    return FilterListResponse.from(filter, type, bookmark);
  }

  @Transactional(readOnly = true)
  public List<Filter> getFiltersByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return filterRepository.findFiltersWithCreatorByIdIn(ids);
  }

  /**
   * 필터 가격 수정
   */
  public Filter updatePrice(Long filterId, Integer newPrice) {
    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));
    filter.updatePrice(newPrice);
    return filterRepository.save(filter);
  }

  /**
   * 필터 삭제 (소프트 딜리트)
   */
  public void deleteFilter(Long filterId) {
    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));
    filter.softDelete();

    // AI 추천 서버에 삭제 요청
    try {
      aiClient.deleteFilter(filterId);
      log.info("🤖 AI Server Deletion Requested for Filter ID: {}", filterId);
    } catch (Exception e) {
      log.warn("⚠️ AI Deletion Failed: {}", e.getMessage());
    }
  }

  /**
   * 필터 사용 & 구매 처리 (동시성 처리 적용)
   */
  @Transactional
  public void useFilter(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);

    // 만약 내가 제작한 필터를 사용하는 경우에는 기록하지 않음
    if (filter.getCreator().getId().equals(user.getId())) {
      return;
    }

    FilterTransaction transaction;

    // 구매한 내역 있으면 가져오고, 없으면 새로 생성
    boolean exists = filterTransactionRepository.existsByBuyerIdAndFilterId(user.getId(), filterId);
    if (exists) {
      transaction = filterTransactionRepository.findByBuyerIdAndFilterId(user.getId(), filterId);
    } else {
      transaction = new FilterTransaction(user, filter.getCreator(), filter);
    }

    // 환불된 필터 차단
    if (transaction.getType() == FilterTransactionType.REFUND) {
      throw new IllegalStateException("환불된 필터는 사용할 수 없습니다.");
    }

    int price = filter.getPrice();
    FilterTransactionType type = transaction.getType();

    // 1. 유료 구매가 필요한 경우 (가격 > 0 이고, 아직 구매 안 함)
    if (price > 0 &&
        (type == FilterTransactionType.INIT || type == FilterTransactionType.FREE_USE)) {

      Point point = pointRepository.findByUserId(user.getId())
          .orElseThrow(() -> new EntityNotFoundException("포인트 정보가 없습니다."));

      if (point.getAmount() < price) {
        throw new IllegalStateException("포인트가 부족합니다.");
      }

      // 트랜잭션 업데이트 & 포인트 차감
      int balance = transaction.buyFirst(price, point.getAmount());
      point.setAmount(balance);

      // [중요] 구매 수 증가 (Repository 쿼리 호출)
      filterRepository.increasePurchaseCountAndAmount(filterId, price);

      log.info("💰 Filter Purchase - Buyer: {}, Filter: {}, Amount: {}",
          user.getId(), filterId, price);

    } else if (price <= 0 && type == FilterTransactionType.INIT) {
      // 2. 무료 필터 최초 사용
      transaction.freeUseFirst();
    } else {
      // 3. 단순 재사용
      transaction.use();
    }

    // 트랜잭션 저장 (기록)
    filterTransactionRepository.save(transaction);

    // [중요] 사용 수 증가 (Repository 쿼리 호출)
    filterRepository.increaseUseCount(filterId);

    log.info("🛒 Filter Use Processed - User: {}, Filter: {}", user.getId(), filterId);
  }
}