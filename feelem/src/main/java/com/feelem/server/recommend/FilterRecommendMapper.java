package com.feelem.server.recommend;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.sticker.entity.FaceStickerPlacement;
import com.feelem.server.recommend.dto.IndexFilterRequest;
import com.feelem.server.recommend.dto.StickerSummary;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FilterRecommendMapper {

  public IndexFilterRequest toIndexRequest(Filter filter) {
    return new IndexFilterRequest(
        String.valueOf(filter.getId()),      // ID는 문자열로 변환
        filter.getEditedImageUrl(),          // S3 URL

        // 태그 리스트 변환 (없으면 빈 리스트)
        filter.getFilterTags().stream()
            .map(ft -> ft.getTag().getName())
            .toList(),

        filter.getColorAdjustments(),        // Map 그대로 전달
        toStickerSummary(filter.getFaceStickerPlacements())
    );
  }

  private StickerSummary toStickerSummary(List<FaceStickerPlacement> placements) {
    // 스티커가 없으면 기본값 반환
    if (placements == null || placements.isEmpty()) {
      return new StickerSummary(0, Collections.emptyList(), false);
    }

    // 1. 배치 구역 변환 (좌표 -> "TOP_LEFT" 등)
    List<String> zones = placements.stream()
        .map(this::convertCoordinatesToZone)
        .distinct() // 중복 구역 제거 (예: 왼쪽 눈에 2개 붙여도 1개로 취급)
        .collect(Collectors.toList());

    // 2. 개수 및 사용 여부
    return new StickerSummary(placements.size(), zones, true);
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

    // 결과 예시: "TOP_LEFT", "MIDDLE_CENTER", "BOTTOM_RIGHT"
    return yZone + "_" + xZone;
  }
}
