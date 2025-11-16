package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.service.FilterSearchService;
import com.feelem.server.domain.filter.dto.FilterListResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/filters")
public class FilterSearchController {

  private final FilterSearchService filterSearchService;

  @Operation(summary = "태그로 필터 검색 (20개씩 페이지네이션)")
  @GetMapping("/search")
  public ResponseEntity<Page<FilterListResponse>> searchFiltersByTag(
      @RequestParam("tag") String tag,
      @RequestParam(defaultValue = "0") int page
  ) {
    Page<Filter> filters = filterSearchService.searchByTag(tag, page);

    // ✅ 새 DTO 매핑 (useCount 포함)]
    // usage: 내 구매 여부, bookmark: 내 북마크 여부
    List<FilterListResponse> dtoList = filters.stream()
        .map(filter -> FilterListResponse.fromEntity(filter, false, false))
        .toList();

    Page<FilterListResponse> responsePage = new PageImpl<>(
        dtoList,
        PageRequest.of(page, filters.getSize()),
        filters.getTotalElements()
    );

    return ResponseEntity.ok(responsePage);
  }
}
