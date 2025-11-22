package com.feelem.server.domain.finance.dto;

import com.feelem.server.domain.finance.entity.FilterTransaction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilterTransactionListResponse {

  private Long id;           // 거래 ID
  private Long filterId;     // 사용/구매한 필터 ID
  private String filterName; // 필터 이름
  private int amount;        // 사용 포인트
  private int balance;       // 거래 후 잔여 포인트
  private String type;       // PURCHASE or FREE_USE
  private String createdAt;

  public static FilterTransactionListResponse from(FilterTransaction tx) {
    return FilterTransactionListResponse.builder()
        .id(tx.getId())
        .filterId(tx.getFilter().getId())
        .filterName(tx.getFilter().getName())
        .amount(tx.getAmount())
        .balance(tx.getBalance())
        .type(tx.getType().name())
        .createdAt(tx.getPurchasedAt().toString())
        .build();
  }
}
