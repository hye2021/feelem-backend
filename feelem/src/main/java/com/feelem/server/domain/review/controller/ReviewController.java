package com.feelem.server.domain.review.controller;

import com.feelem.server.domain.review.dto.ReviewCreateRequest;
import com.feelem.server.domain.review.dto.ReviewResponse;
import com.feelem.server.domain.review.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  // ✅ 리뷰 생성
  @PostMapping
  public ResponseEntity<ReviewResponse> createReview(
      @RequestParam Long userId,
      @RequestBody ReviewCreateRequest request) {

    ReviewResponse response = reviewService.createReview(request);
    return ResponseEntity.ok(response);
  }

  // ✅ 리뷰 1개 조회
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewResponse> getReview(@PathVariable Long reviewId) {

    ReviewResponse response = reviewService.getReviewById(reviewId);

    log.info("⭐ 리뷰 조회: reviewId={}", reviewId);

    return ResponseEntity.ok(response);
  }

  // ✅ 리뷰 미리보기 최대 5개 반환
  @GetMapping("/{filterId}/preview")
  public ResponseEntity<List<ReviewResponse>> getReviewPreview(@PathVariable Long filterId) {

    List<ReviewResponse> previews = reviewService.getReviewPreview(filterId);

    log.info("⭐ 리뷰 미리보기 조회: filterId={}, count={}", filterId, previews.size());

    return ResponseEntity.ok(previews);
  }

  // ✅ 필터에 따른 리뷰 목록 조회
  @GetMapping("/{filterId}")
  public ResponseEntity<Page<ReviewResponse>> getReviews(
      @PathVariable Long filterId,
      @RequestParam(defaultValue = "0") int page) {

    return ResponseEntity.ok(reviewService.getReviewsByFilter(filterId, page));
  }

  // 리뷰 삭제
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {

    reviewService.deleteReview(reviewId);

    log.info("🗑 리뷰 삭제됨: reviewId={}", reviewId);

    return ResponseEntity.noContent().build();
  }
}
