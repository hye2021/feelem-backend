package com.feelem.server.domain.review.controller;

import com.feelem.server.domain.review.dto.ReviewDto;
import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
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

  @Operation(summary = "리뷰 등록", description = "filterId, imageUrl, socialType(선택)을 입력받아 리뷰를 등록합니다.")
  @PostMapping
  public ResponseEntity<ReviewDto.Response> createReview(
      @RequestParam Long userId,
      @RequestBody ReviewDto.CreateRequest request) {

    Review review = reviewService.createReview(request);
    return ResponseEntity.ok(ReviewDto.Response.fromEntity(review));
  }

  @Operation(summary = "리뷰 목록 조회", description = "특정 필터에 등록된 리뷰를 최신순으로 20개씩 반환합니다.")
  @GetMapping("/{filterId}")
  public ResponseEntity<Page<ReviewDto.Response>> getReviews(
      @PathVariable Long filterId,
      @RequestParam(defaultValue = "0") int page) {

    return ResponseEntity.ok(reviewService.getReviewsByFilter(filterId, page));
  }
}
