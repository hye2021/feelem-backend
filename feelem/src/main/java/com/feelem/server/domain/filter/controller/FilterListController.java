package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.dto.FilterListResponse;
import com.feelem.server.domain.filter.dto.FilterSortType;
import com.feelem.server.domain.filter.dto.PriceDisplayType;
import com.feelem.server.domain.filter.dto.SearchType;
import com.feelem.server.domain.filter.entity.Filter;
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

  /** 홈 화면 - 최신 필터 조회 */
  @GetMapping("/recent")
  public ResponseEntity<Page<FilterListResponse>> getRecentFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {

//    log.info("✔️ 홈 화면용 최신 필터 조회: page={}, size={}",
//        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(filterService.getRecentFilters(pageable));
  }

   /** 홈 화면 - 인기 필터 조회*/
  @GetMapping("/hot")
  public ResponseEntity<Page<FilterListResponse>> getHotFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {

//    log.info("✔️ 홈 화면용 인기 필터 조회: page={}, size={}",
//        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(filterService.getHotFilters(pageable));
  }

  /** 홈 화면 - 필터 랜덤 조회*/
  @GetMapping("/random")
  public ResponseEntity<Page<FilterListResponse>> getRandomFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {
//    log.info("✔️ 홈 화면용 랜덤 필터 조회: page={}, size={}",
//        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(filterService.getRandomFilters(pageable));
  }

  /** AI 홈 화면 추천*/
  @GetMapping("/recommend")
  public ResponseEntity<Page<FilterListResponse>> getHomeRecommendations(
      @PageableDefault(size = 20) Pageable pageable
  ) {
//    log.info("🤖 홈 화면 AI 추천 요청: page={}", pageable.getPageNumber());

    // Service는 List를 반환하므로, Page 인터페이스로 감싸서 반환 (API 통일성 유지)
    List<FilterListResponse> result = filterService.getHomeRecommendations(pageable);

    // Total Count는 AI 특성상 정확히 알 수 없거나 무한
    // 현재 페이지 데이터 크기 + (다음 페이지가 있다고 가정하기 위해 넉넉한 수)로 설정하거나
    // 단순히 result.size()로 설정하면 '마지막 페이지'로 인식될 수 있음.
    // 무한 스크롤을 위해 '항상 다음이 있다'고 가정하려면 total을 크게 잡을 것
    long logicalTotal = (result.isEmpty()) ? 0 : 2000L;

    return ResponseEntity.ok(new PageImpl<>(result, pageable, logicalTotal));
  }

  /**검색 (태그 / 자연어 + 정렬)*/
  @GetMapping("/search")
  public ResponseEntity<Page<FilterListResponse>> searchFilters(
      @RequestParam String query,
      @RequestParam(name = "search-type", defaultValue = "NL") SearchType searchType,
      @RequestParam(name = "sort", defaultValue = "ACCURACY") FilterSortType sortType,
      @PageableDefault(size = 20) Pageable pageable
  ) {
//    log.info("🔍 검색 요청: query='{}', type={}, sort={}, page={}",
//        query, searchType, sortType, pageable.getPageNumber());

    List<FilterListResponse> result = filterService.searchFilters(query, searchType, sortType, pageable);

    // AI 무한 루프 페이징이 적용되어 있으므로 Total을 넉넉하게 잡음
    long logicalTotal = (result.isEmpty()) ? 0 : 2000L;

    return ResponseEntity.ok(new PageImpl<>(result, pageable, logicalTotal));
  }

  /** 북마크 목록 조회*/
  @GetMapping("/bookmarks")
  public ResponseEntity<Page<FilterListResponse>> getBookmarks(
      @PageableDefault(size = 20) Pageable pageable
  ) {

    Page<FilterListResponse> responses = filterService.getBookmarkedFilters(pageable);

//    log.info("⭐ 북마크 목록 조회 요청: page={}, size={}",
//        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(responses);
  }

  /** 구매 or 사용한 필터 목록 조회*/
  @GetMapping("/usage")
  public ResponseEntity<Page<FilterListResponse>> getUsedFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {

    Page<FilterListResponse> responses = filterService.getUsedFilters(pageable);

//    log.info("⭐ 사용/구매 필터 조회: page={}, size={}",
//        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(responses);
  }

   /** 내가 제작한 필터 목록*/
  @GetMapping("/my")
  public ResponseEntity<Page<FilterListResponse>> getMyFilters(
      @PageableDefault(size = 20) Pageable pageable
  ) {

    Page<FilterListResponse> responses = filterService.getMyFilters(pageable);

//    log.info("⭐ 내가 올린 필터 조회: page={}, size={}",
//        pageable.getPageNumber(), pageable.getPageSize());

    return ResponseEntity.ok(responses);
  }

}
