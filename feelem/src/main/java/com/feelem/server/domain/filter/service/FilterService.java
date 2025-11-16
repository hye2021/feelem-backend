package com.feelem.server.domain.filter.service;

import com.feelem.server.domain.filter.entity.Bookmark;
import com.feelem.server.domain.filter.repository.BookmarkRepository;
import com.feelem.server.domain.filter.dto.FilterDto;
import com.feelem.server.domain.filter.dto.FilterListResponse;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.entity.FilterSticker;
import com.feelem.server.domain.filter.entity.FilterTag;
import com.feelem.server.domain.filter.entity.Tag;
import com.feelem.server.domain.filter.repository.FilterRepository;
import com.feelem.server.domain.filter.repository.FilterStickerRepository;
import com.feelem.server.domain.filter.repository.FilterTagRepository;
import com.feelem.server.domain.filter.repository.TagRepository;
import com.feelem.server.domain.finance.entity.FilterTransaction;
import com.feelem.server.domain.finance.entity.FilterTransactionType;
import com.feelem.server.domain.finance.repository.FilterTransactionRepository;
import com.feelem.server.domain.sticker.entity.Sticker;
import com.feelem.server.domain.sticker.repository.StickerRepository;
import com.feelem.server.domain.user.entity.Point;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.PointRepository;
import com.feelem.server.domain.user.service.UserService;
import com.feelem.server.recommend.FilterIndexedEvent;
import com.feelem.server.recommend.dto.IndexFilterRequest;
import com.feelem.server.recommend.dto.StickerSummary;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

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
public class FilterService {

  private final FilterRepository filterRepository;
  private final StickerRepository stickerRepository;
  private final TagRepository tagRepository;
  private final FilterTagRepository filterTagRepository;
  private final FilterStickerRepository filterStickerRepository;
  private final BookmarkRepository bookmarkRepository;
  private final FilterTransactionRepository filterTransactionRepository;
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher;
  private final PointRepository pointRepository;

  // ================================================================
  // 1) 필터 생성
  // ================================================================
  public Filter createFilter(FilterDto.CreateRequest request) {
    User creator = userService.getCurrentUser();

    Filter filter = Filter.builder()
        .creator(creator)
        .name(request.getName())
        .price(request.getPrice())
        .colorAdjustments(request.getColorAdjustments())
        .originalImageUrl(request.getOriginalImageUrl())
        .editedImageUrl(request.getEditedImageUrl())
        .aspectX(request.getAspectX())
        .aspectY(request.getAspectY())
        .build();

    Filter savedFilter = filterRepository.save(filter);

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
    List<FilterSticker> savedStickers = new ArrayList<>();
    if (request.getStickers() != null && !request.getStickers().isEmpty()) {
      for (FilterDto.CreateRequest.StickerPlacement sp : request.getStickers()) {

        Sticker sticker = stickerRepository.findById(sp.getStickerId())
            .orElseThrow(() -> new EntityNotFoundException("Sticker not found: " + sp.getStickerId()));

        FilterSticker filterSticker = FilterSticker.builder()
            .filter(savedFilter)
            .sticker(sticker)
            .placementType(sp.getPlacementType())
            .scale(sp.getScale())
            .x(sp.getX())
            .y(sp.getY())
            .rotation(sp.getRotation())
            .anchor(sp.getAnchor())
            .build();

        savedStickers.add(filterStickerRepository.save(filterSticker));
      }
    }

    // FastAPI 인덱싱 이벤트 발행
    IndexFilterRequest payload = buildIndexRequestPayload(savedFilter, tagNames, savedStickers);
    eventPublisher.publishEvent(new FilterIndexedEvent(payload));

    return savedFilter;
  }

  /** 인덱싱 요청 Payload 생성 */
  private IndexFilterRequest buildIndexRequestPayload(Filter filter, List<String> tagNames, List<FilterSticker> stickers) {

    List<String> placementTypes = stickers.stream()
        .map(fs -> fs.getPlacementType().name())
        .distinct()
        .collect(Collectors.toList());

    List<String> stickerTypes = stickers.stream()
        .map(fs -> fs.getSticker().getStickerType().name())
        .distinct()
        .collect(Collectors.toList());

    StickerSummary summary = new StickerSummary(
        stickers.size(),
        placementTypes,
        stickerTypes
    );

    return new IndexFilterRequest(
        String.valueOf(filter.getId()),
        filter.getEditedImageUrl(),
        tagNames,
        filter.getColorAdjustments(),
        summary
    );
  }


  // ================================================================
  // 2) 필터 조회 (상세)
  // ================================================================
  @Transactional(readOnly = true)
  public FilterDto.Response getFilter(Long filterId) {
    Filter filter = filterRepository.findByIdAndIsDeletedFalse(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));

    List<String> tags = filter.getFilterTags().stream()
        .map(ft -> ft.getTag().getName())
        .collect(Collectors.toList());

    List<FilterDto.CreateRequest.StickerPlacement> stickers = filter.getFilterStickers().stream()
        .map(fs -> FilterDto.CreateRequest.StickerPlacement.builder()
            .stickerId(fs.getSticker().getId())
            .placementType(fs.getPlacementType())
            .scale(fs.getScale())
            .x(fs.getX())
            .y(fs.getY())
            .anchor(fs.getAnchor())
            .build())
        .collect(Collectors.toList());

    return new FilterDto.Response(filter, tags, stickers);
  }


  @Transactional(readOnly = true)
  public Filter findById(Long filterId) {
    return filterRepository.findByIdAndIsDeletedFalse(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));
  }

  /** 홈 화면용 - 최신 등록 필터 페이징 조회 */
  @Transactional(readOnly = true)
  public Page<FilterListResponse> getRecentFilters(Pageable pageable) {
    return filterRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc(pageable)
        .map(filter -> FilterListResponse.from(filter, false, false));
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
    } else {
      bookmarkRepository.save(new Bookmark(user, filter));
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
    User user = userService.getCurrentUser();
    Filter filter = findById(filterId);

    // 이미 구매한 적 있는지 확인 (중복 방지)
    boolean exists = filterTransactionRepository.existsByBuyerIdAndFilterId(user.getId(), filterId);
    if (exists) {
      return;  // 이미 구매/사용한 필터는 다시 구매하지 않음
    }

    int price = filter.getPrice();

    // 1) 무료 필터 처리
    if (price == 0) {

      FilterTransaction tx = FilterTransaction.builder()
          .type(FilterTransactionType.FREE_USE)
          .amount(0)                // 금액 0
          .balance(0)               // 잔액 의미 없음 → 0
          .buyer(user)
          .seller(filter.getCreator())
          .filter(filter)
          .usedAt(LocalDateTime.now())
          .build();

      filterTransactionRepository.save(tx);
      return;
    }

    // 2) 유료 필터 구매 처리

    // 유저 포인트 조회
    Point point = pointRepository.findByUserId(user.getId())
        .orElseThrow(() -> new EntityNotFoundException("포인트 정보가 없습니다."));

    int currentAmount = point.getAmount();

    // 포인트 부족 시 예외
    if (currentAmount < price) {
      throw new IllegalStateException("포인트가 부족합니다.");
    }

    // 포인트 차감
    int newBalance = currentAmount - price;
    point.setAmount(newBalance);

    // 트랜잭션 생성 (유료 구매)
    FilterTransaction tx = FilterTransaction.builder()
        .type(FilterTransactionType.PURCHASE)
        .amount(price)
        .balance(newBalance)
        .buyer(user)
        .seller(filter.getCreator())
        .filter(filter)
        .usedAt(LocalDateTime.now())
        .build();

    filterTransactionRepository.save(tx);
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
