package com.feelem.server.recommend.dto;

import java.util.List;
public record HomeRecommendRequest(List<String> filterIds) {}
