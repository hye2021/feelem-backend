package com.feelem.server.domain.filter.dto;

public enum FilterSortType {
  ACCURACY,       // 추천순 (AI 전용)
  POPULARITY,     // 인기순 (저장 많은 순)
  LATEST,         // 최신순
  LOW_PRICE,      // 낮은 가격순
  REVIEW_COUNT    // 리뷰 많은 순
}
