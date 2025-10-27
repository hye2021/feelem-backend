package com.feelem.server.domain.archive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.feelem.server.domain.filter.Filter;
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
@Table(
    name = "filter_usages",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "filter_id"})
    }
)
public class FilterUsage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 필터를 구매하거나 사용한 유저
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private User user;

  // 구매/사용한 필터
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "filter_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Filter filter;

  // 거래 유형 (구매 / 사용 / 환불 등)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UsageType type;

  // 결제 금액 (무료 사용 시 0)
  @Column(name = "price_point", nullable = false)
  private int pricePoint;

  // 거래 상태 (PURCHASED, CANCELLED, REFUNDED 등)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UsageStatus status;

  @Column(name = "used_at", nullable = false)
  private LocalDateTime usedAt;

  @Builder
  public FilterUsage(User user, Filter filter, UsageType type, int pricePoint, UsageStatus status) {
    this.user = user;
    this.filter = filter;
    this.type = type;
    this.pricePoint = pricePoint;
    this.status = status;
    this.usedAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    if (this.usedAt == null) this.usedAt = LocalDateTime.now();
  }
}