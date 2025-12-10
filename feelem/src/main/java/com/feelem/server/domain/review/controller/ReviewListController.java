package com.feelem.server.domain.review.controller;

import com.feelem.server.domain.review.dto.ReviewResponse;
import com.feelem.server.domain.review.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/review-lists")
@RequiredArgsConstructor
public class ReviewListController {

  private final ReviewService reviewService;

  /** 리뷰 미리보기 최대 5개 반환*/
  @GetMapping("/{filterId}/preview")
  public ResponseEntity<List<ReviewResponse>> getReviewPreview(@PathVariable Long filterId) {

    List<ReviewResponse> previews = reviewService.getReviewPreview(filterId);

//    log.info("⭐ 리뷰 미리보기 조회: filterId={}, count={}", filterId, previews.size());

    return ResponseEntity.ok(previews);
  }

  /** 리뷰 목록 조회 (페이징) */
  @GetMapping("/{filterId}")
  public ResponseEntity<Page<ReviewResponse>> getReviews(
      @PathVariable Long filterId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

//    log.info("⭐ 필터 리뷰 목록 조회: filterId={}, page={}, size={}", filterId, page, size);

    return ResponseEntity.ok(reviewService.getReviewsByFilter(filterId, page, size));
  }

}
