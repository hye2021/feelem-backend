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
import java.util.List;

@RequiredArgsConstructor
@Service
public class FilterService {

  private final FilterRepository filterRepository;
  private final TagRepository tagRepository;
  private final StickerRepository stickerRepository;
  private final FilterTagRepository filterTagRepository;
  private final FilterStickerRepository filterStickerRepository;
  private final UserRepository userRepository;

  /**
   * 새로운 필터를 생성하는 메서드
   * @param requestDto 필터 생성에 필요한 데이터
   * @param userId 필터를 생성하는 사용자의 ID
   * @return 생성된 필터의 ID
   */
  @Transactional
  public Long createFilter(FilterDto.CreateRequest requestDto, Long userId) {
    // 1. 필터를 생성할 사용자를 조회합니다. 없으면 예외를 발생시킵니다.
    User creator = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("요청한 사용자를 찾을 수 없습니다: " + userId));

    // todo
    // 2. DTO로부터 Filter 엔티티를 생성합니다.
    Filter newFilter = Filter.builder()
        .creator(creator)
        .name(requestDto.getName())
        .price(requestDto.getPrice())
        .colorAdjustments(requestDto.getColorAdjustments())
        .build();

    // 3. Filter를 먼저 저장하여 ID를 부여받습니다.
    filterRepository.save(newFilter);

    // 4. 태그 목록을 처리합니다. (null이 아닐 경우에만)
    List<String> tags = requestDto.getTags();
    if (tags != null && !tags.isEmpty()) {
      tags.forEach(tagName -> {
        // DB에 이미 태그가 있으면 가져오고, 없으면 새로 생성해서 저장합니다.
        Tag tag = tagRepository.findByName(tagName)
            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));

        // filter와 tag의 관계를 조인 테이블에 저장합니다.
        filterTagRepository.save(FilterTag.builder().filter(newFilter).tag(tag).build());
      });
    }

    // 5. 스티커 목록을 처리합니다. (null이 아닐 경우에만)
    List<FilterDto.StickerPlacementRequest> stickers = requestDto.getStickers();
    if (stickers != null && !stickers.isEmpty()) {
      stickers.forEach(stickerRequest -> {
        // 재사용할 스티커를 DB에서 조회합니다. 없으면 예외를 발생시킵니다.
        Sticker sticker = stickerRepository.findById(stickerRequest.getStickerId())
            .orElseThrow(() -> new EntityNotFoundException("요청한 스티커를 찾을 수 없습니다: " + stickerRequest.getStickerId()));

        // filter와 sticker의 관계 및 배치 정보를 조인 테이블에 저장합니다.
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