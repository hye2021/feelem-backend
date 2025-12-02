package com.feelem.server.domain.finance.dto;

import java.util.Date;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalesGraphResponse {
  // 필터의 총 판매 포인트 얼마
  private Long totalSalesPoints;
  // 필터의 총 판매 수량 얼마
  private Long totalSalesCount;
  // 그래프 리스트
  private Map<String, Long> salesGraphData; // 최근 일주일: <"10-24",500>, 최근 한 달: <"10-24", 500>, 최근 일 년: <"2023-10", 500>
}
