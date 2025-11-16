package com.feelem.server.domain.finance.dto;

import com.feelem.server.domain.finance.entity.CashTransaction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CashTransactionListResponse {

  private Long id;           // 거래 ID
  private double cash;       // 충전 금액 (현금)
  private int point;         // 충전되어 얻은 포인트
  private int balance;       // 거래 후 잔여 포인트
  private String type;       // CHARGE
  private String createdAt;  // 거래 일시

  public static CashTransactionListResponse from(CashTransaction tx) {
    return CashTransactionListResponse.builder()
        .id(tx.getId())
        .cash(tx.getCash())
        .point(tx.getPoint())
        .balance(tx.getBalance())
        .type(tx.getType().name())
        .createdAt(tx.getCreatedAt().toString())
        .build();
  }
}

