package com.feelem.server.recommend;

import java.util.List;
public record HomeRecommendRequest(List<String> filterIds) {}
