package com.feelem.server.recommend.dto;

import java.util.List;
import java.util.Map;

// Python 서버의 IndexFilterRequest 모델과 일치해야 합니다.
public record IndexFilterRequest(
    String filter_id,

    // ✅ [추가] 필터 이름 (AI 서버 메타데이터 저장용)
    String name,

    String image_url,
    List<String> tags,
    Map<String, Double> color_adjustments,
    StickerSummary sticker_summary
) {
  public record StickerSummary(
      int total_count,
      List<String> placement_types,
      List<String> sticker_types
  ) {}
}