package com.feelem.server.recommend.dto;

import java.util.List;
import java.util.Map;

public record IndexFilterRequest(
    String filter_id,
    String image_url,
    List<String> tags,
    Map<String, Double> color_adjustments,
    StickerSummary sticker_summary
) {}
