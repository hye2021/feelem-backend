package com.feelem.server.recommend;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.sticker.entity.FaceStickerPlacement;
import com.feelem.server.recommend.dto.IndexFilterRequest;
import com.feelem.server.recommend.dto.IndexFilterRequest.StickerSummary; // DTO 내부 레코드 import 확인 필요
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FilterRecommendMapper {

  public IndexFilterRequest toIndexRequest(Filter filter) {
    return new IndexFilterRequest(
        String.valueOf(filter.getId()),      // 1. filter_id
        filter.getName(),                    // 2. name (✅ 누락된 필드 추가)
        filter.getEditedImageUrl(),          // 3. image_url

        // 4. tags
        filter.getFilterTags().stream()
            .map(ft -> ft.getTag().getName())
            .toList(),

        filter.getColorAdjustments(),        // 5. color_adjustments
        toStickerSummary(filter.getFaceStickerPlacements()) // 6. sticker_summary
    );
  }

  // 반환 타입을 IndexFilterRequest 내부의 StickerSummary로 가정 (또는 별도 파일이면 해당 타입)
  private StickerSummary toStickerSummary(List<FaceStickerPlacement> placements) {
    // 스티커가 없으면 빈 리스트 반환
    if (placements == null || placements.isEmpty()) {
      return new StickerSummary(0, Collections.emptyList(), Collections.emptyList());
    }

    // 1. 배치 구역 변환 (좌표 -> "TOP_LEFT" 등)
    List<String> zones = placements.stream()
        .map(this::convertCoordinatesToZone)
        .distinct()
        .collect(Collectors.toList());

    // 2. 스티커 종류(ID) 변환 (✅ Boolean -> List<String> 으로 변경)
    // AI가 어떤 스티커가 쓰였는지 식별할 수 있도록 ID를 문자열로 변환하여 전달
    List<String> stickerTypes = placements.stream()
        .map(p -> String.valueOf(p.getSticker().getId()))
        .distinct()
        .collect(Collectors.toList());

    return new StickerSummary(placements.size(), zones, stickerTypes);
  }

  // 📍 핵심 로직: 실수 좌표(0.0 ~ 1.0)를 3x3 그리드 영역 이름으로 변환
  private String convertCoordinatesToZone(FaceStickerPlacement p) {
    // null 방어 코드 (기본값 중앙)
    double x = (p.getRelX() != null) ? p.getRelX() : 0.5;
    double y = (p.getRelY() != null) ? p.getRelY() : 0.5;

    String xZone;
    if (x < 0.33) xZone = "LEFT";
    else if (x > 0.66) xZone = "RIGHT";
    else xZone = "CENTER";

    String yZone;
    if (y < 0.33) yZone = "TOP";
    else if (y > 0.66) yZone = "BOTTOM";
    else yZone = "MIDDLE";

    return yZone + "_" + xZone;
  }
}