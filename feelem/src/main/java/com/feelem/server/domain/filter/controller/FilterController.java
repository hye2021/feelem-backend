package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.dto.FilterDto;
import com.feelem.server.domain.filter.dto.FilterListResponse;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/filters")
public class FilterController {

  private final FilterService filterService;

  // ---------------------------------------------------------
  // 기존 코드 유지 (필터 생성)
  // ---------------------------------------------------------
  @PostMapping
  public ResponseEntity<FilterDto.Response> createFilter(@RequestBody FilterDto.CreateRequest request) {
    Filter filter = filterService.createFilter(request);
    FilterDto.Response response = filterService.getFilter(filter.getId());

    log.info("✔️ 필터가 생성되었습니다: {}", response);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // ---------------------------------------------------------
  // 기존 코드 유지 (필터 상세 조회)
  // ---------------------------------------------------------
  @GetMapping("/{filterId}")
  public ResponseEntity<FilterDto.Response> getFilter(@PathVariable Long filterId) {

    log.info("✔️ 필터가 조회되었습니다: filterId={}", filterId);

    return ResponseEntity.ok(filterService.getFilter(filterId));
  }

  // ---------------------------------------------------------
  // 기존 코드 유지 (가격 업데이트)
  // ---------------------------------------------------------
  @PutMapping("/{filterId}/price")
  public ResponseEntity<Void> updatePrice(
      @PathVariable Long filterId,
      @RequestBody FilterDto.UpdatePriceRequest request
  ) {
    filterService.updatePrice(filterId, request.getPrice());

    log.info("✔️ 필터 가격이 업데이트되었습니다: filterId={}, price={}", filterId, request.getPrice());

    return ResponseEntity.ok().build();
  }

  // ---------------------------------------------------------
  // 기존 코드 유지 (필터 삭제)
  // ---------------------------------------------------------
  @DeleteMapping("/{filterId}")
  public ResponseEntity<Void> deleteFilter(@PathVariable Long filterId) {
    filterService.deleteFilter(filterId);

    log.info("✔️ 필터가 삭제되었습니다: filterId={}", filterId);

    return ResponseEntity.noContent().build();
  }

  // ---------------------------------------------------------
  // 🔥 새로운 기능: 홈 화면 최신 필터 조회
  // ---------------------------------------------------------
  @GetMapping("/recent")
  public ResponseEntity<Page<FilterListResponse>> getRecentFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {

    log.info("✔️ 홈 화면용 최신 필터 조회: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(filterService.getRecentFilters(pageable));
  }
}
