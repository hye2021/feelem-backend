package com.feelem.server.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewCreateRequest {
  private Long filterId;
  private String imageUrl;
  private String socialType; // instagram / x
}
