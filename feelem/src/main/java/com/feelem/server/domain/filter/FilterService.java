package com.feelem.server.domain.filter;

import com.feelem.server.domain.sticker.Sticker;
import com.feelem.server.domain.sticker.StickerRepository;
import com.feelem.server.domain.user.User;
import com.feelem.server.domain.user.UserRepository;
import com.feelem.server.global.dto.FilterDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FilterService {

  private final FilterRepository filterRepository;
  private final TagRepository tagRepository;
  private final StickerRepository stickerRepository;
  private final FilterTagRepository filterTagRepository;
  private final FilterStickerRepository filterStickerRepository;
  private final UserRepository userRepository;

  @Transactional
  public Long createFilter(FilterDto.CreateRequest requestDto, Long userId) {
    User creator = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    Filter newFilter = Filter.builder()
        .creator(creator)
        .name(requestDto.getName())
        .description(requestDto.getDescription())
        .price(requestDto.getPrice())
        .isPublic(requestDto.getIsPublic())
        .colorAdjustments(requestDto.getColorAdjustments())
        .build();
    filterRepository.save(newFilter);

    // 태그 처리
    if (requestDto.getTags() != null) {
      requestDto.getTags().forEach(tagName -> {
        Tag tag = tagRepository.findByName(tagName)
            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
        filterTagRepository.save(FilterTag.builder().filter(newFilter).tag(tag).build());
      });
    }

    // 스티커 처리
    if (requestDto.getStickers() != null) {
      requestDto.getStickers().forEach(stickerRequest -> {
        Sticker sticker = stickerRepository.findById(stickerRequest.getStickerId())
            .orElseThrow(() -> new EntityNotFoundException("스티커를 찾을 수 없습니다: " + stickerRequest.getStickerId()));

        filterStickerRepository.save(FilterSticker.builder()
            .filter(newFilter)
            .sticker(sticker)
            .placementInfo(stickerRequest.getPlacementInfo())
            .build());
      });
    }

    return newFilter.getId();
  }
}