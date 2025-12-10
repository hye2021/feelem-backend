package com.feelem.server.domain.finance.controller;

import com.feelem.server.domain.finance.dto.SalesGraphResponse;
import com.feelem.server.domain.finance.dto.SalesListResponse;
import com.feelem.server.domain.finance.dto.SalesPeriod;
import com.feelem.server.domain.finance.dto.SalesSortType;
import com.feelem.server.domain.finance.dto.SalesTotalResponse;
import com.feelem.server.domain.finance.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sales")
public class SalesController {
  private final SalesService salesService;

  /**
   * 1. 판매 인사이트 - 종합 판매 수치 조회
   */
  @GetMapping("/total")
  public ResponseEntity<SalesTotalResponse> getTotalSales(
      @RequestParam(name = "period", defaultValue = "WEEK") SalesPeriod period
  ) {
    SalesTotalResponse response = salesService.getTotalSales(period);
    return ResponseEntity.ok(response);
  }

  /**
   * 2. 판매 인사이트 - 판매중인 필터 목록 조회
   */
  @GetMapping("/lists")
  public ResponseEntity<Page<SalesListResponse>> getSalesList(
      @RequestParam(name = "sortBy", defaultValue = "RECENT") SalesSortType sortBy,
      @PageableDefault(size = 10) Pageable pageable
  ) {
    Page<SalesListResponse> response = salesService.getSalesFilterList(sortBy, pageable);
    return ResponseEntity.ok(response);
  }

  /**
   * 3. 판매 인사이트 - 개별 필터에 대한 수치 조회 (그래프 데이터 등)
   */
  @GetMapping("/{filterId}")
  public ResponseEntity<SalesGraphResponse> getFilterSalesDetail(
      @PathVariable("filterId") Long filterId,
      @RequestParam(name = "period", defaultValue = "WEEK") SalesPeriod period
  ) {
    SalesGraphResponse response = salesService.getFilterSalesDetail(filterId, period);
    return ResponseEntity.ok(response);
  }
}
