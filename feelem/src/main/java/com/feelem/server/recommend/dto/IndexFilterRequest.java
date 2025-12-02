package com.feelem.server.recommend.dto;

import java.util.List;
import java.util.Map;

// Python 서버의 IndexFilterRequest 모델과 일치해야 합니다.
public record IndexFilterRequest(
    String filter_id,
    String name,
    String image_url,
    List<String> tags,
    Map<String, Double> color_adjustments,
    StickerSummary sticker_summary
) {}