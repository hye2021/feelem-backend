package com.feelem.server.recommend;

import com.feelem.server.recommend.dto.IndexFilterRequest;
import com.feelem.server.recommend.dto.RecommendResponse;
import com.feelem.server.recommend.dto.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RecommendServingClient {

  private final WebClient webClient;

  public RecommendServingClient(@Qualifier("recommendationWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  // 1. 인덱싱 요청
  public void indexFilter(IndexFilterRequest request) {
    webClient.post()
        .uri("/admin/index")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .subscribe(
            null,
            error -> log.error("❌ AI Indexing Failed for FilterID: {}", request.filter_id(), error)
        );
  }

  // 2. 삭제 요청
  public void deleteFilter(Long filterId) {
    webClient.delete()
        .uri("/admin/filter/" + filterId)
        .retrieve()
        .bodyToMono(Void.class)
        .subscribe(
            null,
            error -> log.error("❌ AI Deletion Failed for FilterID: {}", filterId, error)
        );
  }

  // 3. 홈 추천 요청
  public List<String> getHomeRecommendations(List<String> likedFilterIds, int page) {
    Map<String, Object> body = Map.of("filter_ids", likedFilterIds);

    try {
      RecommendResponse response = webClient.post()
          .uri(uriBuilder -> uriBuilder.path("/recommend/home")
              .queryParam("page", page)
              .build())
          .bodyValue(body)
          .retrieve()
          .bodyToMono(RecommendResponse.class)
          .block();

      return (response != null) ? response.recommended_ids() : Collections.emptyList();
    } catch (Exception e) {
      log.error("⚠️ AI Home Recommendation Failed: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  // 4. 검색 요청 (수정됨: size 추가, q -> query 변경)
  public List<String> getSearchResults(String query, int page, int size) {
    try {
      SearchResponse response = webClient.get()
          .uri(uriBuilder -> uriBuilder.path("/search")
              // ⚠️ 중요: Python 함수 인자가 'query'이므로 'q' 대신 'query'를 써야 합니다.
              .queryParam("query", query)
              .queryParam("page", page)
              // ✅ 추가: 하이브리드 검색을 위해 200개를 한 번에 요청할 수 있도록 size 파라미터 추가
              .queryParam("size", size)
              .build())
          .retrieve()
          .bodyToMono(SearchResponse.class)
          .block();

      return (response != null) ? response.search_results() : Collections.emptyList();
    } catch (Exception e) {
      log.error("⚠️ AI Search Failed: {}", e.getMessage());
      return Collections.emptyList();
    }
  }
}