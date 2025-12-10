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
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@Slf4j
public class RecommendServingClient {

  private final WebClient webClient;

  public RecommendServingClient(@Qualifier("recommendationWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  // 1. 인덱싱 요청
  public void indexFilter(IndexFilterRequest request) {
    try {
      webClient.post()
          .uri("/admin/index")
          .bodyValue(request)
          .retrieve()
          .bodyToMono(Void.class)
          .block(); // 서버 응답이 올 때까지 기다림 (동기 처리)

    } catch (WebClientResponseException e) {
      log.error("❌ AI Indexing Failed (Status: {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw e; // 컨트롤러가 실패를 알 수 있도록 예외를 다시 던짐
    } catch (Exception e) {
      log.error("❌ AI Indexing Error: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  // 2. 삭제 요청
  public void deleteFilter(Long filterId) {
    try {
      webClient.delete()
          .uri("/admin/filter/" + filterId)
          .retrieve()
          .bodyToMono(Void.class)
          .block(); // 삭제가 완료될 때까지 기다림

      log.info("🗑️ AI Server Filter Deleted: {}", filterId);

    } catch (WebClientResponseException e) {
      log.error("❌ AI Deletion Failed (Status: {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
      // 삭제 실패는 비즈니스 로직(DB 삭제)을 롤백시킬 정도는 아니므로 예외를 던지지는 않음
    } catch (Exception e) {
      log.error("❌ AI Deletion Connection Error: {}", e.getMessage());
    }
  }

  // 3. 홈 추천 요청
  public List<String> getHomeRecommendations(List<String> likedFilterIds, int page, int size) {
    Map<String, Object> body = Map.of("filter_ids", likedFilterIds);

    try {
      RecommendResponse response = webClient.post()
          .uri(uriBuilder -> uriBuilder.path("/recommend/home")
              .queryParam("page", page)
              .queryParam("size", size)
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
              .queryParam("query", query)
              .queryParam("page", page)
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