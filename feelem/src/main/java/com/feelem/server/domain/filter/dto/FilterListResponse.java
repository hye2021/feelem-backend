package com.feelem.server.domain.filter.dto;

import com.feelem.server.domain.filter.entity.Filter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilterListResponse {

  private Long id;
  private String name;
  private String thumbnailUrl;
  private String creator;
  private int pricePoint;
  private Long useCount;
  private boolean usage; // 구매 여부
  private boolean bookmark; // 북마크 여부

  public static FilterListResponse from(Filter filter, boolean usage, boolean bookmark) {
    return FilterListResponse.builder()
        .id(filter.getId())
        .name(filter.getName())
        .thumbnailUrl(filter.getEditedImageUrl())
        .creator(filter.getCreator().getNickname())
        .pricePoint(filter.getPrice())
        .useCount(filter.getUseCount())
        .usage(usage)
        .bookmark(bookmark)
        .build();
  }
}

