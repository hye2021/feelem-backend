package com.feelem.server.recommend;

import java.util.List;
import java.util.Map;

public record IndexFilterRequest(
    String filterId,
    String imageUrl,
    List<String> tags,
    Map<String, Double> colorAdjustments,
    StickerSummary stickerSummary
) {}
