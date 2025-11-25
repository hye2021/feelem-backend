package com.feelem.server.domain.review.dto;

import com.feelem.server.domain.filter.dto.PriceDisplayType;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.user.entity.SocialType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyReviewResponse {
  private Long id;
  private String imageUrl;
  private String reviewerNickname;
  private String socialType;   // NONE, INSTAGRAM, X
  private String socialValue;  // 대표 소셜 ID 값
  private LocalDateTime createdAt;

  private Long filterId;
  private String filterName;
  private String priceDisplayType; // 가격 표시 유형: NONE, PURCHASED, NUMBER
  private int pricePoint; // 필터 가격

  public static MyReviewResponse fromEntity(Review review, Filter filter, PriceDisplayType displayType) {
    // 소셜 설정
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

    // 빌더
    return MyReviewResponse.builder()
        .id(review.getId())
        .imageUrl(review.getImageUrl())
        .reviewerNickname(review.getReviewer().getNickname())
        .socialType(socialType)
        .socialValue(socialValue)
        .createdAt(review.getCreatedAt())
        .filterId(filter.getId())
        .filterName(filter.getName())
        .priceDisplayType(type.toString())
        .pricePoint(filter.getPrice())
        .build();
  }
}
