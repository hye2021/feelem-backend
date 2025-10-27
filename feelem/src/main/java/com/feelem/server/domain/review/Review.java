package com.feelem.server.domain.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.feelem.server.domain.filter.Filter;
import com.feelem.server.domain.user.Social;
import com.feelem.server.domain.user.User;
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
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private User reviewer;

  // 리뷰 대상 필터
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "filter_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Filter filter;

  // 리뷰 이미지 (S3 URL)
  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  // 유저가 선택한 소셜 계정 (선택적, null 가능)
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "social_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Social social;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @Builder
  public Review(User reviewer, Filter filter, String imageUrl, Social social) {
    this.reviewer = reviewer;
    this.filter = filter;
    this.imageUrl = imageUrl;
    this.social = social;
    this.createdAt = LocalDateTime.now();
  }
}
