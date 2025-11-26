package com.feelem.server.recommend.dto;

import java.util.List;

public record RecommendResponse(
    List<String> recommended_ids
) {}
