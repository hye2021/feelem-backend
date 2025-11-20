package com.feelem.server.domain.review.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.user.entity.Social;
import com.feelem.server.domain.user.entity.SocialType;
import com.feelem.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "reviews")
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 리뷰 작성자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id", nullable = false)
  private User reviewer;

  // 리뷰 대상 필터
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "filter_id", nullable = false)
  private Filter filter;

  // 리뷰 이미지 (S3 URL)
  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  // sns 아이디 표기
  @Column(name = "social_type")
  private SocialType socialType;

  // 유저가 선택한 소셜 계정 (선택적, null 가능)
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "social_id")
  private Social social;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public Review(User reviewer, Filter filter, String imageUrl, SocialType socialType,
      Social social) {
    this.reviewer = reviewer;
    this.filter = filter;
    this.imageUrl = imageUrl;
    this.socialType = socialType;
    this.social = social;
    this.createdAt = LocalDateTime.now();
  }
}
