package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.dto.FilterListResponse;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.service.FilterSearchService;
import com.feelem.server.domain.filter.service.FilterService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/filter-lists")
public class FilterListController {
  private final FilterService filterService;
  private final FilterSearchService filterSearchService;

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

  // ---------------------------------------------------------
  // 🔥 홈 화면 인기 필터 조회
  // ---------------------------------------------------------
  @GetMapping("/hot")
  public ResponseEntity<Page<FilterListResponse>> getHotFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {

    log.info("✔️ 홈 화면용 인기 필터 조회: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(filterService.getHotFilters(pageable));
  }

  // ---------------------------------------------------------
  // 2) 북마크 목록 조회
  // ---------------------------------------------------------
  @GetMapping("/bookmarks")
  public ResponseEntity<Page<FilterListResponse>> getBookmarks(
      @PageableDefault(size = 20) Pageable pageable
  ) {

    Page<FilterListResponse> responses = filterService.getBookmarkedFilters(pageable);

    log.info("⭐ 북마크 목록 조회 요청: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(responses);
  }

  // ---------------------------------------------------------
  // 4) 구매 or 사용한 필터 목록 조회 (20개 페이징)
  // ---------------------------------------------------------
  @GetMapping("/usage")
  public ResponseEntity<Page<FilterListResponse>> getUsedFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {

    Page<FilterListResponse> responses = filterService.getUsedFilters(pageable);

    log.info("⭐ 사용/구매 필터 조회: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(responses);
  }

  // ---------------------------------------------------------
  // 내가 제작한 필터 목록
  // ---------------------------------------------------------
  @GetMapping("my")
  public ResponseEntity<Page<FilterListResponse>> getMyFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {

    Page<FilterListResponse> responses = filterService.getMyFilters(pageable);

    log.info("⭐ 내가 올린 필터 조회: page={}, size={}",
        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(responses);
  }

  // ---------------------------------------------------------
  // 태그검색
  // ---------------------------------------------------------
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
        .map(filter -> FilterListResponse.from(filter, false, false))
        .toList();

    Page<FilterListResponse> responsePage = new PageImpl<>(
        dtoList,
        PageRequest.of(page, filters.getSize()),
        filters.getTotalElements()
    );

    return ResponseEntity.ok(responsePage);
  }

}
