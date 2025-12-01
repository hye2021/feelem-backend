package com.feelem.server.domain.review.controller;

import com.feelem.server.domain.review.dto.MyReviewResponse;
import com.feelem.server.domain.review.dto.ReviewResponse;
import com.feelem.server.domain.review.service.ReviewService;
import com.feelem.server.domain.upload.service.UploadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  /** 리뷰 작성 : 이미지 업로드 + 리뷰 생성 */
  private final UploadService uploadService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ReviewResponse> createReview(
      @RequestParam("file") MultipartFile file,
      @RequestParam("filterId") Long filterId,
      @RequestParam("socialType") String socialType
  ) {
    try {
      // 1️⃣ 이미지 S3 업로드 (기존에 있던 uploadFilterReview 메서드 활용)
      String imageUrl = uploadService.uploadFilterReview(file);
      log.info("✔️ 리뷰 이미지 업로드 완료: {}", imageUrl);

      // 2️⃣ 리뷰 생성 서비스 호출 (URL과 데이터 전달)
      ReviewResponse response = reviewService.createReview(filterId, socialType, imageUrl);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("❌ 리뷰 생성 실패", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /** 내가 작성한 리뷰 목록 조회 (아카이브용) : 20개씩 페이징*/
  @GetMapping("/my")
  public ResponseEntity<Page<MyReviewResponse>> getMyReviews(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    log.info("⭐ 내가 작성한 리뷰 목록 조회: page={}, size={}", page, size);

    return ResponseEntity.ok(reviewService.getMyReviews(page, size));
  }

  /** 리뷰 1개 조회*/
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewResponse> getReview(@PathVariable Long reviewId) {

    ReviewResponse response = reviewService.getReviewById(reviewId);

    log.info("⭐ 리뷰 조회: reviewId={}", reviewId);

    return ResponseEntity.ok(response);
  }

  /** 리뷰 삭제*/
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {

    reviewService.deleteReview(reviewId);

    log.info("🗑 리뷰 삭제됨: reviewId={}", reviewId);

    return ResponseEntity.noContent().build();
  }
}
