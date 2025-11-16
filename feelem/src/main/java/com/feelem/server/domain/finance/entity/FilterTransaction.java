package com.feelem.server.domain.finance.entity;

import com.feelem.server.domain.filter.entity.Filter;
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
@Table(
    name = "filter_transactions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"buyer_id", "filter_id"}) // 같은 사용자가 동일한 필터를 중복 구매/사용할 수 없음
    }
)
public class FilterTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 거래 유형 (구매 / 사용 / 환불 등)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FilterTransactionType type;

  // 결제 금액 (무료 사용 시 0)
  @Column(name = "amount", nullable = false)
  private int amount;

  @Column(name = "balance", nullable = false)
  private int balance;

  // 필터를 구매하거나 사용한 유저
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id", nullable = false)
  private User buyer;

  // 필터 판매자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  // 구매/사용한 필터
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "filter_id", nullable = false)
  private Filter filter;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "used_at", nullable = false)
  private LocalDateTime usedAt;

  @Builder
  public FilterTransaction(
      FilterTransactionType type,
      int amount,
      int balance,
      User buyer,
      User seller,
      Filter filter,
      LocalDateTime usedAt
  ) {
    this.type = type;
    this.amount = amount;
    this.balance = balance;
    this.buyer = buyer;
    this.seller = seller;
    this.filter = filter;
    this.usedAt = usedAt;
    this.createdAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    if (this.usedAt == null) this.usedAt = LocalDateTime.now();
  }
}