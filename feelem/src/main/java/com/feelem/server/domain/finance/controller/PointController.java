package com.feelem.server.domain.finance.controller;

import com.feelem.server.domain.finance.service.PointService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointController {

  private final PointService pointService;

  /** 포인트 충전 */
  @PostMapping("/charge")
  public ResponseEntity<Map<String, Integer>> chargePoint(@RequestParam int cash) {
    int newBalance = pointService.chargePoint(cash);
    return ResponseEntity.ok(Map.of("point", newBalance));
  }
}
