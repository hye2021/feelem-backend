package com.feelem.server.recommend.dto;

import java.util.List;

public record StickerSummary(int count, List<String> placementTypes, List<String> stickerTypes) {}
