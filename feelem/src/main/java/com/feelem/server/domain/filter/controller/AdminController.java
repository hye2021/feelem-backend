package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.repository.FilterRepository;
import com.feelem.server.recommend.FilterRecommendMapper;
import com.feelem.server.recommend.RecommendServingClient;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

  private final FilterRepository filterRepository;
  private final RecommendServingClient aiClient;
  private final FilterRecommendMapper aiMapper;

  // 🚀 [중요] 기존 필터 데이터를 전부 AI 서버로 전송하는 API
  @PostMapping("/sync-filters")
  public ResponseEntity<String> syncAllFiltersToAi() {
    log.info("🔄 기존 필터 데이터 AI 서버 동기화 시작...");

    // 1. 삭제되지 않은 모든 필터 조회
    List<Filter> allFilters = filterRepository.findAllByIsDeletedFalse();
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failCount = new AtomicInteger();

    // 2. 루프 돌면서 하나씩 인덱싱 요청
    for (Filter filter : allFilters) {
      try {
        aiClient.indexFilter(aiMapper.toIndexRequest(filter));
        successCount.getAndIncrement();
        // 너무 빠르게 보내면 과부하 걸릴 수 있으니 살짝 텀을 줌 (선택사항)
        Thread.sleep(50);
      } catch (Exception e) {
        log.error("❌ 필터 ID {} 인덱싱 실패: {}", filter.getId(), e.getMessage());
        failCount.getAndIncrement();
      }
    }

    String result = String.format("✅ 동기화 완료! 성공: %d건, 실패: %d건", successCount.get(), failCount.get());
    log.info(result);
    return ResponseEntity.ok(result);
  }
}
