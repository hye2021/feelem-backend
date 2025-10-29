package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.dto.FilterSearchResponse;
import com.feelem.server.domain.filter.service.FilterHomeService;
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
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

  private final FilterHomeService homeFilterService;

  /**
   * ✅ 홈 화면 - 최신 필터 조회 (20개씩 페이징)
   * 기본 정렬: 최신 등록 순(createdAt DESC)
   */
  @GetMapping("/filters")
  public ResponseEntity<Page<FilterSearchResponse>> getHomeFilters(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Page<FilterSearchResponse> filters = homeFilterService.getRecentFilters(pageable);
    return ResponseEntity.ok(filters);
  }
}
