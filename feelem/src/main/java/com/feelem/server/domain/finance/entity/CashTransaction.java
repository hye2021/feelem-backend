package com.feelem.server.domain.finance.entity;

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
@Table(name = "cash_transactions")
public class CashTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CashTransactionType type;

  @Column(name = "cash", nullable = false)
  private double cash;

  @Column(name = "point", nullable = false)
  private int point;

  @Column(name = "balance", nullable = false)
  private int balance;

  @Column(length = 255)
  private String description;

  // 거래 발생 시각
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @Builder
  public CashTransaction(
      User user,
      CashTransactionType type,
      double cash,
      int point,
      int afterPoint,
      String description) {
    this.user = user;
    this.type = type;
    this.cash = cash;
    this.point = point;
    this.balance= afterPoint;
    this.description = description;
    this.createdAt = LocalDateTime.now();
  }
}

