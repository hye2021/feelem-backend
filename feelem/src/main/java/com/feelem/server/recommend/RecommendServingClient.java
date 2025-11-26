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

  // ✅ 변경됨: Config에 등록된 'recommendationWebClient' 빈(Bean)을 주입받음
  // (@Value로 URL을 직접 받는 대신, 이미 설정된 WebClient 객체를 받습니다)
  public RecommendServingClient(@Qualifier("recommendationWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  // 1. 인덱싱 요청 (비동기)
  public void indexFilter(IndexFilterRequest request) {
    webClient.post()
        .uri("/admin/index") // baseUrl이 설정돼 있으므로 뒷부분만 작성
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Void.class)
        .subscribe(
            null,
            error -> log.error("❌ AI Indexing Failed for FilterID: {}", request.filter_id(), error)
        );
  }

  // 2. 삭제 요청 (비동기)
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

  // 3. 홈 추천 요청 (동기)
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

  // 4. 검색 요청 (동기)
  public List<String> getSearchResults(String query, int page) {
    try {
      SearchResponse response = webClient.get()
          .uri(uriBuilder -> uriBuilder.path("/search")
              .queryParam("q", query)
              .queryParam("page", page)
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