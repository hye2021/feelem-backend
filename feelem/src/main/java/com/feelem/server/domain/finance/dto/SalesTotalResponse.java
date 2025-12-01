package com.feelem.server.domain.finance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalesTotalResponse {
  // 정산 예정 금액
  private Long settlementAmount;
  // 총 판매 수량
  private Long totalSales;
}
