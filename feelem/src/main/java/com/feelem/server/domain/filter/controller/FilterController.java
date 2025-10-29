package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.service.FilterService;
import com.feelem.server.domain.filter.dto.FilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/filters")
public class FilterController {

  private final FilterService filterService;

  @PostMapping
  public ResponseEntity<FilterDto.Response> createFilter(@RequestBody FilterDto.CreateRequest request) {
    Filter filter = filterService.createFilter(request);
    FilterDto.Response response = filterService.getFilter(filter.getId());

    log.info("✔️ 필터가 생성되었습니다: {}", response);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{filterId}")
  public ResponseEntity<FilterDto.Response> getFilter(@PathVariable Long filterId) {

    log.info("✔️ 필터가 조회되었습니다: filterId={}", filterId);

    return ResponseEntity.ok(filterService.getFilter(filterId));
  }

  @PutMapping("/{filterId}/price")
  public ResponseEntity<Void> updatePrice(
      @PathVariable Long filterId,
      @RequestBody FilterDto.UpdatePriceRequest request
  ) {
    filterService.updatePrice(filterId, request.getPrice());

    log.info("✔️ 필터 가격이 업데이트되었습니다: filterId={}, price={}", filterId, request.getPrice());

    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{filterId}")
  public ResponseEntity<Void> deleteFilter(@PathVariable Long filterId) {
    filterService.deleteFilter(filterId);

    log.info("✔️ 필터가 삭제되었습니다: filterId={}", filterId);

    return ResponseEntity.noContent().build();
  }
}
