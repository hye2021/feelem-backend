package com.feelem.server.recommend.controller;

import com.feelem.server.domain.filter.entity.Filter; // [변경] Filter 엔티티 Import
import com.feelem.server.domain.filter.service.FilterService;
import com.feelem.server.domain.filter.dto.FilterSearchResponse; // [변경] 사용자 DTO Import
import com.feelem.server.recommend.dto.HomeRecommendRequest;
import com.feelem.server.recommend.RecommendClient;
import com.feelem.server.recommend.dto.RecommendResponse;
import com.feelem.server.recommend.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map; // [추가]
import java.util.function.Function; // [추가]
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/filters") // API 기본 경로
public class RecommendController {

  private final RecommendClient recommendClient;
  private final FilterService filterService;

  /**
   * 기능 2: 텍스트 검색 API
   * (GET /api/filters/search?q=...&page=...)
   */
  @GetMapping("/search")
  public Mono<List<FilterSearchResponse>> searchFilters(
      @RequestParam("q") String query,
      @RequestParam(value = "page", defaultValue = "0") int page
  ) {
    // 1. FastAPI에서 ID 리스트(String)를 받음
    return recommendClient.searchText(query, page)
        .map(SearchResponse::searchResults)
        // 2. ID 리스트를 DTO 리스트로 변환 (공통 헬퍼 메서드 사용)
        .flatMap(this::mapIdsToFilterSearchResponse);
  }

  /**
   * 기능 1: 홈 화면 필터 추천 API
   * (POST /api/filters/recommend/home?page=...)
   */
  @PostMapping("/recommend/home")
  public Mono<List<FilterSearchResponse>> recommendHomeFilters(
      @RequestBody HomeRecommendRequest requestBody, // 최근 사용 필터 ID 2개
      @RequestParam(value = "page", defaultValue = "0") int page
  ) {
    // 1. FastAPI에서 ID 리스트(String)를 받음
    return recommendClient.getHomeRecommendations(requestBody.filterIds(), page)
        .map(RecommendResponse::recommendedIds)
        // 2. ID 리스트를 DTO 리스트로 변환 (공통 헬퍼 메서드 사용)
        .flatMap(this::mapIdsToFilterSearchResponse);
  }

  /**
   * [추가] ID 리스트를 받아 DB에서 조회 후 DTO 리스트로 변환하는 공통 헬퍼 메서드
   * (N+1 문제 해결 및 추천 순서 보장)
   */
  private Mono<List<FilterSearchResponse>> mapIdsToFilterSearchResponse(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return Mono.just(List.of());
    }

    List<Long> longIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());

    // 2. DB 조회 (JPA)는 블로킹 I/O이므로 Mono.fromCallable로 감싸
    // WebFlux의 이벤트 루프를 차단하지 않도록 합니다.
    return Mono.fromCallable(() -> {

      // 3. FilterService의 새 메서드로 Filter+Creator를 한방에 조회
      List<Filter> filters = filterService.getFiltersByIds(longIds);

      // 4. (중요) 추천 순서를 보장하기 위해 Map으로 변환
      // (DB의 IN 쿼리는 ID 순서를 보장하지 않음)
      Map<Long, Filter> filterMap = filters.stream()
          .collect(Collectors.toMap(Filter::getId, Function.identity()));

      // 5. FastAPI가 정해준 'ID 순서대로' DTO 리스트를 생성
      return longIds.stream()
          .map(filterMap::get) // ID 순서대로 Filter 객체를 찾음
          .filter(java.util.Objects::nonNull) // (혹시 삭제된 필터가 있으면 null)
          .map(FilterSearchResponse::from) // 사용자 DTO로 변환
          .collect(Collectors.toList());
    });
  }
}