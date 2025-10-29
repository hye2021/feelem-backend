package com.feelem.server.domain.review.dto;

import com.feelem.server.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class ReviewDto {

  @Getter
  @Builder
  public static class CreateRequest {
    private Long filterId;
    private String imageUrl;
    private String socialType; // instagram / x
  }

  @Getter
  @Builder
  public static class Response {
    private Long id;
    private String imageUrl;
    private String reviewerNickname;
    private String socialType;   // 대표 소셜 종류
    private String socialValue;  // 대표 소셜 ID 값
    private LocalDateTime createdAt;

    public static Response fromEntity(Review review) {
      String socialType = null;
      String socialValue = null;

      if (review.getSocial() != null) {
        if (review.getSocial().getInstagramId() != null) {
          socialType = "instagram";
          socialValue = review.getSocial().getInstagramId();
        } else if (review.getSocial().getXId() != null) {
          socialType = "x";
          socialValue = review.getSocial().getXId();
        }
      }

      return Response.builder()
          .id(review.getId())
          .imageUrl(review.getImageUrl())
          .reviewerNickname(review.getReviewer().getNickname())
          .socialType(socialType)
          .socialValue(socialValue)
          .createdAt(review.getCreatedAt())
          .build();
    }
  }
}
