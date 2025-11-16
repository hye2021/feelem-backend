package com.feelem.server.domain.finance.controller;

import com.feelem.server.domain.finance.dto.CashTransactionListResponse;
import com.feelem.server.domain.finance.dto.FilterTransactionListResponse;
import com.feelem.server.domain.finance.service.PointHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointHistoryController {

  private final PointHistoryService pointHistoryService;

  /** 포인트 충전 내역 조회 (50개씩) */
  @GetMapping("/charge/history")
  public ResponseEntity<Page<CashTransactionListResponse>> getChargeHistory(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {

    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(pointHistoryService.getChargeHistory(pageable));
  }

  /** 포인트 사용 내역 조회 (50개씩) */
  @GetMapping("/usage")
  public ResponseEntity<Page<FilterTransactionListResponse>> getUsageHistory(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {

    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(pointHistoryService.getUsageHistory(pageable));
  }
}
