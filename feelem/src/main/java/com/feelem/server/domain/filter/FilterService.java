package com.feelem.server.domain.filter;

import com.feelem.server.recommend.FilterIndexedEvent;
import com.feelem.server.recommend.IndexFilterRequest;
import com.feelem.server.domain.sticker.Sticker;
import com.feelem.server.domain.sticker.StickerRepository;
import com.feelem.server.domain.user.User;
import com.feelem.server.domain.user.UserService;
import com.feelem.server.global.dto.FilterDto;
import com.feelem.server.recommend.StickerSummary;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher; // [추가]
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // [추가]
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
  private final UserService userService;
  private final ApplicationEventPublisher eventPublisher; // [추가]

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

    // 2. 태그 등록
    List<String> tagNames = request.getTags(); // [변경] 나중에 사용하기 위해 변수 할당
    if (tagNames != null && !tagNames.isEmpty()) {
      for (String tagName : tagNames) {
        Tag tag = tagRepository.findByName(tagName)
            .orElseGet(() -> tagRepository.save(new Tag(tagName)));
        filterTagRepository.save(new FilterTag(savedFilter, tag));
      }
    } else {
      tagNames = List.of(); // null 방지
    }

    // 3. 스티커 배치 등록
    List<FilterSticker> savedStickers = new ArrayList<>(); // [변경] 저장된 스티커 목록 수집
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

        savedStickers.add(filterStickerRepository.save(filterSticker)); // [변경] 저장 후 리스트에 추가
      }
    }

    // [추가] 4. 인덱싱 이벤트 발행
    // (데이터가 DB에 저장된 후, 트랜잭션이 커밋될 때 이벤트를 발행합니다)
    IndexFilterRequest payload = buildIndexRequestPayload(savedFilter, tagNames, savedStickers);
    eventPublisher.publishEvent(new FilterIndexedEvent(payload));

    return savedFilter;
  }

  /**
   * [추가] 저장된 엔티티 정보를 바탕으로 FastAPI 서버에 보낼 DTO를 생성합니다.
   */
  private IndexFilterRequest buildIndexRequestPayload(Filter filter, List<String> tagNames, List<FilterSticker> stickers) {

    // 1. 스티커 요약 정보 생성
    List<String> placementTypes = stickers.stream()
        .map(fs -> fs.getPlacementType().name()) // "ABSOLUTE", "FACE_TRACKING"
        .distinct()
        .collect(Collectors.toList());

    List<String> stickerTypes = stickers.stream()
        .map(fs -> fs.getSticker().getStickerType().name()) // "IMAGE", "AI", "BRUSH"
        .distinct()
        .collect(Collectors.toList());

    StickerSummary summary = new StickerSummary(
        stickers.size(),
        placementTypes,
        stickerTypes
    );

    // 2. 최종 DTO 생성
    return new IndexFilterRequest(
        String.valueOf(filter.getId()),
        filter.getEditedImageUrl(),
        tagNames, // createFilter에서 수집한 태그 이름 리스트
        filter.getColorAdjustments(),
        summary
    );
  }

  // --- (이하 getFilter, updatePrice, deleteFilter 메서드는 기존과 동일) ---

  @Transactional(readOnly = true)
  public FilterDto.Response getFilter(Long filterId) {
    Filter filter = filterRepository.findByIdAndIsDeletedFalse(filterId)
        .orElseThrow(() -> new EntityNotFoundException("Filter not found"));

    // 태그 리스트
    List<String> tags = filter.getFilterTags().stream()
        .map(ft -> ft.getTag().getName())
        .collect(Collectors.toList());

    // 스티커 배치 리스트
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

  /**
   * [추가] ID 리스트로 Filter 엔티티 리스트를 효율적으로 조회합니다.
   * (N+1 문제 해결용)
   */
  @Transactional(readOnly = true)
  public List<Filter> getFiltersByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    // 위에서 추가한 Repository 메서드 호출
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
}