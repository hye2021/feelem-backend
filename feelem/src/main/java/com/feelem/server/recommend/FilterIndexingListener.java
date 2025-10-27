package com.feelem.server.recommend;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class FilterIndexingListener {

  private final RecommendClient recommendationClient;

  public FilterIndexingListener(RecommendClient recommendationClient) {
    this.recommendationClient = recommendationClient;
  }

  // 트랜잭션이 성공적으로 '커밋'된 후에만 이 메서드를 실행
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFilterIndexedEvent(FilterIndexedEvent event) {
    System.out.println("Transaction committed. Requesting indexing for filter: "
        + event.payload().filterId());

    // 비동기로 FastAPI 서버 호출
    recommendationClient.requestIndexing(event.payload());
  }
}
