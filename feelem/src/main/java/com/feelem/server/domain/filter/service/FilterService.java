package com.feelem.server.domain.filter.service;

import com.feelem.server.domain.filter.dto.FilterCreateRequest;
import com.feelem.server.domain.filter.dto.FilterCreateRequest.FaceSticker;
import com.feelem.server.domain.filter.dto.FilterResponse;
import com.feelem.server.domain.filter.dto.FilterResponse.FaceStickerResponse;
import com.feelem.server.domain.filter.entity.Bookmark;
import com.feelem.server.domain.filter.repository.BookmarkRepository;
import com.feelem.server.domain.filter.dto.FilterListResponse;
import com.feelem.server.domain.filter.entity.Filter;
//import com.feelem.server.domain.filter.entity.FilterSticker;
import com.feelem.server.domain.filter.entity.FilterTag;
import com.feelem.server.domain.filter.entity.Tag;
import com.feelem.server.domain.filter.repository.FilterRepository;
//import com.feelem.server.domain.filter.repository.FilterStickerRepository;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
//  private final FilterStickerRepository filterStickerRepository;
  private final BookmarkRepository bookmarkRepository;
  private final FilterTransactionRepository filterTransactionRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final PointRepository pointRepository;
  private final FaceStickerPlacementRepository faceStickerPlacementRepository;
  private final FilterTransactionRepository filterTransactionRepo;

  // ================================================================
  // 1) 필터 생성
  // ================================================================
  public Filter createFilter(FilterCreateRequest request) {
    log.info("🚀 [FilterService] createFilter 호출됨");

    // 1. 스티커 리스트 상태 로그 (가장 중요!)
    if (request.getStickers() == null) {
      log.error("🚨 [문제발견] request.getStickers()가 NULL입니다! (JSON 필드명 불일치 가능성 높음)");
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
    } else {
      tagNames = List.of();
    }

    // 스티커 배치 등록
    List<FaceStickerPlacement> savedStickers = new ArrayList<>();

    // 2. 반복문 진입 로그
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

          // 상세 데이터 로그
          log.info("   💾 스티커 저장 시도 - Sticker ID: {}, relX: {}", placement.getStickerId(), placement.getRelX());

          faceStickerPlacementRepository.save(facePlacement);
          savedStickers.add(facePlacement);

        } catch (Exception e) {
          log.error("❌ 스티커 저장 중 에러 발생 (ID: {}): {}", placement.getStickerId(), e.getMessage());
          throw e; // 트랜잭션 롤백을 위해 예외 다시 던짐
        }
      }
    } else {
      log.info("⏭️ 저장할 스티커가 없어서 반복문을 건너뜁니다.");
    }

    log.info("✅ 필터 생성 최종 완료. 저장된 스티커 수: {}", savedStickers.size());

    return savedFilter;
  }

  /** 인덱싱 요청 Payload 생성 */
//  private IndexFilterRequest buildIndexRequestPayload(Filter filter, List<String> tagNames, List<FilterSticker> stickers) {
//
//    List<String> placementTypes = stickers.stream()
//        .map(fs -> fs.getPlacementType().name())
//        .distinct()
//        .collect(Collectors.toList());
//
//    List<String> stickerTypes = stickers.stream()
//        .map(fs -> fs.getSticker().getStickerType().name())
//        .distinct()
//        .collect(Collectors.toList());
//
//    StickerSummary summary = new StickerSummary(
//        stickers.size(),
//        placementTypes,
//        stickerTypes
//    );
//
//    return new IndexFilterRequest(
//        String.valueOf(filter.getId()),
//        filter.getEditedImageUrl(),
//        tagNames,
//        filter.getColorAdjustments(),
//        summary
//    );
//  }


  // ================================================================
  // 2) 필터 조회 (상세)
  // ================================================================
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
    boolean isUsed = filterTransactionRepo.existsByBuyerIdAndFilterId(currentUser.getId(), filterId);
    // 현재 사용자가 북마크한 필터인지 확인
    boolean isBookmarked = bookmarkRepository.existsByUserAndFilter(currentUser, filter);

    return new FilterResponse(filter, isMine, isUsed, isBookmarked, tags, stickers);
  }


  @Transactional(readOnly = true)
  public Filter findById(Long filterId) {
    return filterRepository.findByIdAndIsDeletedFalse(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));
  }

  /** 홈 화면용 - 최신 등록 필터 페이징 조회 */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getRecentFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = filterRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc(pageable);

    // 반한된 리스트 로그 찍기
    for (Filter filter : page.getContent()) {
      log.info("➡️ Recent Filter ID: {}, Created At: {}", filter.getId(), filter.getCreatedAt());
    }

    return page.map(filter -> toFilterListResponse(filter, user));
  }

  /** 홈 화면용 - 인기 등록 필터 페이징 조회 */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getHotFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = filterRepository.findAllByIsDeletedFalseOrderBySaveCountDesc(pageable);

    // 반한된 리스트 로그 찍기
    for (Filter filter : page.getContent()) {
      log.info("➡️ Hot Filter ID: {}, Save Count: {}", filter.getId(), filter.getSaveCount());
    }

    return page.map(filter -> toFilterListResponse(filter, user));
  }

  /** 아카이브- 내가 올린 필터 목록 조회 */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getMyFilters(Pageable pageable) {
    User user = userService.getCurrentUser();
    Page<Filter> page = filterRepository.findAllByCreatorIdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId(), pageable);

    return page.map(filter -> toFilterListResponse(filter, user));
  }

  // ================================================================
  // 3) 북마크 기능 (BookmarkService → FilterService로 통합)
  // ================================================================
  @Transactional
  public void toggleBookmark(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);

    boolean exists = bookmarkRepository.existsByUserAndFilter(user, filter);

    if (exists) {
      bookmarkRepository.deleteByUserAndFilter(user, filter);
      filter.decreaseSaveCount();
    } else {
      bookmarkRepository.save(new Bookmark(user, filter));
      filter.increaseSaveCount();
    }
  }

  @Transactional(readOnly = true)
  public boolean isBookmarked(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);
    return bookmarkRepository.existsByUserAndFilter(user, filter);
  }


  @Transactional
  public void addBookmark(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);

    if (bookmarkRepository.existsByUserAndFilter(user, filter)) {
      throw new IllegalStateException("Already bookmarked");
    }

    bookmarkRepository.save(new Bookmark(user, filter));
  }

  @Transactional
  public void removeBookmark(Long filterId) {
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);

    Bookmark bookmark = bookmarkRepository.findByUserAndFilter(user, filter)
        .orElseThrow(() -> new EntityNotFoundException("Bookmark not found"));

    bookmarkRepository.delete(bookmark);
  }

  @Transactional(readOnly = true)
  public Page<FilterListResponse> getBookmarkedFilters(Pageable pageable) {
    User user = userService.getCurrentUser();

    Page<Filter> page = bookmarkRepository.findBookmarkedFilters(user.getId(), pageable);

    return page.map(filter -> {
      boolean usage = filterTransactionRepository.existsByBuyerIdAndFilterId(user.getId(), filter.getId());
      return FilterListResponse.from(filter, usage, true);
    });
  }


  // ================================================================
  // 4) 공통 변환 메서드 (Filter → FilterListResponse)
  // ================================================================
  private FilterListResponse toFilterListResponse(Filter filter, User user) {
    boolean bookmark = bookmarkRepository.existsByUserAndFilter(user, filter);
    boolean usage = filterTransactionRepository.existsByBuyerIdAndFilterId(user.getId(), filter.getId());
    return FilterListResponse.from(filter, usage, bookmark);
  }


  // ================================================================
  // 5) 기타 기능
  // ================================================================
  @Transactional(readOnly = true)
  public List<Filter> getFiltersByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return filterRepository.findFiltersWithCreatorByIdIn(ids);
  }

  public Filter updatePrice(Long filterId, Integer newPrice) {
    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));
    filter.updatePrice(newPrice);
    return filterRepository.save(filter);
  }

  public void deleteFilter(Long filterId) {
    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));
    filter.softDelete();
  }

  // ================================================================
  // 6) 필터 구매, 사용 기능
  // ================================================================
  @Transactional
  public void useFilter(Long filterId) {
    FilterTransaction transaction;

    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);

    // 만약 내가 제작한 필터를 사용하는 경우에는 기록하지 않음
    if (filter.getCreator().getId().equals(user.getId())) {
      return;
    }

    // 구매한 내역 있으면 업데이트, 없으면 새로 생성
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

    // 현재 transaction의 type에 따라 처리
    int price = filter.getPrice();
    FilterTransactionType type = transaction.getType();

    if (price > 0 &&
        (type == FilterTransactionType.INIT || type == FilterTransactionType.FREE_USE)) {
      // 유료 필터로 처음 구매하거나, 무료에서 유료로 전환하는 경우
      // 유저 포인트 조회
      Point point = pointRepository.findByUserId(user.getId())
          .orElseThrow(() -> new EntityNotFoundException("포인트 정보가 없습니다."));
      int currentAmount = point.getAmount();
      // 포인트 부족 시 예외
      if (currentAmount < price) {
        throw new IllegalStateException("포인트가 부족합니다.");
      }
      // 트랜잭션 업데이트 (유료 사용)
      int balance = transaction.buyFirst(price, currentAmount);
      // 포인트 차감
      point.setAmount(balance);
    } else if (price <= 0 &&
        type == FilterTransactionType.INIT) {
    } else {
      transaction.use();
    }
    filter.increaseUseCount();

    // 구매자, 현재 타입, 구매 가격, 잔액 출력
    log.info("🛒 Filter Use - Buyer ID: {}, Type: {}, Amount: {}, Balance: {}",
        user.getId(), transaction.getType(), transaction.getAmount(), transaction.getBalance());

    filterTransactionRepository.save(transaction);
  }

  @Transactional(readOnly = true)
  public Page<FilterListResponse> getUsedFilters(Pageable pageable) {
    User user = userService.getCurrentUser();

    Page<Filter> page = filterTransactionRepository.findUsedOrPurchasedFilters(user.getId(), pageable);

    return page.map(filter -> {
      boolean bookmark = bookmarkRepository.existsByUserAndFilter(user, filter);
      boolean usage = true; // 이미 구매/사용한 목록이므로 true
      return FilterListResponse.from(filter, usage, bookmark);
    });
  }

}
