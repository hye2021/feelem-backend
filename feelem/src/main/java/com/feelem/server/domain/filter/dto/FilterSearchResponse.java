package com.feelem.server.domain.filter.dto;

import com.feelem.server.domain.filter.entity.Filter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilterSearchResponse {

  private Long id;
  private String name;
  private String thumbnailUrl;
  private String creatorName;
  private int pricePoint;
  private Long useCount;

  public static FilterSearchResponse from(Filter filter) {
    return FilterSearchResponse.builder()
        .id(filter.getId())
        .name(filter.getName())
        .thumbnailUrl(filter.getEditedImageUrl())
        .creatorName(filter.getCreator().getNickname())
        .pricePoint(filter.getPrice())
        .useCount(filter.getUseCount())
        .build();
  }
}

