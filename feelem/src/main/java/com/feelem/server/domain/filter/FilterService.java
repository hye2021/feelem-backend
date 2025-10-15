package com.feelem.server.domain.filter;

import com.feelem.server.domain.sticker.Sticker;
import com.feelem.server.domain.sticker.StickerRepository;
import com.feelem.server.domain.user.User;
import com.feelem.server.domain.user.UserService;
import com.feelem.server.global.dto.FilterDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    if (request.getTags() != null && !request.getTags().isEmpty()) {
      for (String tagName : request.getTags()) {
        Tag tag = tagRepository.findByName(tagName)
            .orElseGet(() -> tagRepository.save(new Tag(tagName)));
        filterTagRepository.save(new FilterTag(savedFilter, tag));
      }
    }

    // 3. 스티커 배치 등록
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
            .anchor(sp.getAnchor())
            .build();

        filterStickerRepository.save(filterSticker);
      }
    }

    return savedFilter;
  }

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
