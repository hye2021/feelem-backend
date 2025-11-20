package com.feelem.server.domain.review.dto;

import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.user.entity.SocialType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
public class ReviewResponse {
  private boolean isMine;

  private Long id;
  private String imageUrl;
  private String reviewerNickname;
  private String socialType;   // 대표 소셜 종류
  private String socialValue;  // 대표 소셜 ID 값
  private LocalDateTime createdAt;

  public static ReviewResponse fromEntity(Review review, boolean isMine) {
    SocialType type = review.getSocialType();
    String socialType = type.toString();

    String socialValue = null;
    if (type == SocialType.NONE) {
      socialValue = "";
    } else if (type == SocialType.INSTAGRAM) {
      socialValue = review.getSocial().getInstagramId();
    } else if (type == SocialType.X) {
      socialValue = review.getSocial().getXId();
    }

    return ReviewResponse.builder()
        .id(review.getId())
        .imageUrl(review.getImageUrl())
        .reviewerNickname(review.getReviewer().getNickname())
        .socialType(socialType)
        .socialValue(socialValue)
        .createdAt(review.getCreatedAt())
        .build();
  }
}
