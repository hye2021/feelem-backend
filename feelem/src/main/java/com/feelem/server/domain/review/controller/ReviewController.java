package com.feelem.server.domain.review.controller;

import com.feelem.server.domain.review.dto.ReviewDto;
import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.review.service.ReviewService;
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

  @PostMapping
  public ResponseEntity<ReviewDto.Response> createReview(
      @RequestParam Long userId,
      @RequestBody ReviewDto.CreateRequest request) {

    Review review = reviewService.createReview(request);
    return ResponseEntity.ok(ReviewDto.Response.fromEntity(review));
  }

  @GetMapping("/{filterId}")
  public ResponseEntity<Page<ReviewDto.Response>> getReviews(
      @PathVariable Long filterId,
      @RequestParam(defaultValue = "0") int page) {

    return ResponseEntity.ok(reviewService.getReviewsByFilter(filterId, page));
  }
}
