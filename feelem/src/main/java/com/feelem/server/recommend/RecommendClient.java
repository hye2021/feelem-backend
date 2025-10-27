package com.feelem.server.recommend;

import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RecommendClient {

  private final WebClient webClient;

  // WebClientConfig에서 생성된 Bean 주입
  public RecommendClient(WebClient recommendationWebClient) {
    this.webClient = recommendationWebClient;
  }

  /**
   * 홈 화면 필터 추천 요청 (비동기)
   */
  public Mono<RecommendResponse> getHomeRecommendations(List<String> filterIds, int page) {
    HomeRecommendRequest requestBody = new HomeRecommendRequest(filterIds);

    return webClient.post()
        .uri(uriBuilder -> uriBuilder
            .path("/recommend/home")
            .queryParam("page", page)
            .build())
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(RecommendResponse.class)
        .onErrorResume(e -> {
          // API 호출 실패 시 로깅하고 빈 응답 반환 (예외 방지)
          System.err.println("Error calling recommendation server: " + e.getMessage());
          return Mono.just(new RecommendResponse(List.of()));
        });
  }

  /**
   * 텍스트 검색 요청 (비동기)
   */
  public Mono<SearchResponse> searchText(String query, int page) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/search")
            .queryParam("q", query)
            .queryParam("page", page)
            .build())
        .retrieve()
        .bodyToMono(SearchResponse.class)
        .onErrorResume(e -> {
          System.err.println("Error calling search server: " + e.getMessage());
          return Mono.just(new SearchResponse(List.of()));
        });
  }

  /**
   * (비동기) FastAPI 서버에 필터 인덱싱을 요청합니다.
   * @Async: 이 작업이 실패해도 메인 스레드(필터 생성)는 성공하도록 비동기로 실행
   */
  @Async
  public void requestIndexing(IndexFilterRequest requestBody) {
    webClient.post()
        .uri("/admin/index")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(Void.class) // 응답 본문은 무시
        .doOnError(e -> {
          // 비동기 호출이므로 에러는 여기서 로깅해야 합니다.
          System.err.println("Failed to request indexing for filter "
              + requestBody.filterId() + ": " + e.getMessage());
        })
        .subscribe(); // 비동기 Mono 실행
  }
}
