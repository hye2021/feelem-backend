package com.feelem.server.recommend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
public class FilterIndexingListener {

  private final RecommendClient recommendationClient;

  public FilterIndexingListener(RecommendClient recommendationClient) {
    this.recommendationClient = recommendationClient;
  }

  // ✅ 트랜잭션이 커밋된 뒤에 실행
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFilterIndexedEvent(FilterIndexedEvent event) {
    String filterId = event.payload().filterId();

    log.info("✔️ 트랜잭션 커밋 완료. 인덱싱 요청 시작: filterId={}", filterId);

    try {
      // 비동기 호출 (실패해도 서버는 영향받지 않음)
      recommendationClient.requestIndexing(event.payload());
      log.info("📤 인덱싱 요청 전송 완료 (filterId={})", filterId);

    } catch (Exception e) {
      // ✅ 추천 서버가 꺼져 있거나 연결 오류 발생 시
      log.warn("⚠️ 추천 서버 호출 실패 (filterId={}) - 서버가 꺼져 있거나 연결 불가: {}",
          filterId, e.getMessage(), e);
    }
  }
}
