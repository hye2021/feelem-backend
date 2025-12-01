package com.feelem.server.domain.review.service;

import com.feelem.server.domain.filter.dto.PriceDisplayType;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.repository.FilterRepository;
import com.feelem.server.domain.finance.repository.FilterTransactionRepository;
import com.feelem.server.domain.review.dto.MyReviewResponse;
import com.feelem.server.domain.review.dto.ReviewResponse;
import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.review.repository.ReviewRepository;
import com.feelem.server.domain.user.entity.Social;
import com.feelem.server.domain.user.entity.SocialType;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.SocialRepository;
import com.feelem.server.domain.user.repository.UserRepository;
import com.feelem.server.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final FilterRepository filterRepository;
  private final UserRepository userRepository;
  private final SocialRepository socialRepository;
  private final UserService userService;
  private final FilterTransactionRepository filterTransactionRepository;

  // ✅ 리뷰 생성
  @Transactional
  public ReviewResponse createReview(Long filterId, String socialTypeStr, String imageUrl) {

    Long userId = userService.getCurrentUser().getId();

    User reviewer = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 필터입니다."));

    // 소셜 정보 조회 (없으면 null)
    Social social = socialRepository.findByUser(reviewer).orElse(null);

    SocialType requestedType = SocialType.fromString(socialTypeStr);

    // 소셜 연동 유효성 검사
    if (requestedType != SocialType.NONE) {
      if (social == null) {
        throw new IllegalArgumentException("해당 유저의 소셜 정보가 설정되지 않았습니다.");
      }
      if ((requestedType == SocialType.INSTAGRAM && (social.getInstagramId() == null || social.getInstagramId().isEmpty())) ||
          (requestedType == SocialType.X && (social.getXId() == null || social.getXId().isEmpty()))) {
        throw new IllegalArgumentException("요청한 소셜 타입에 해당하는 ID 정보가 없습니다.");
      }
    }

    // 리뷰 엔티티 생성
    Review review = Review.builder()
        .reviewer(reviewer)
        .filter(filter)
        .imageUrl(imageUrl) // 업로드된 URL 사용
        .socialType(requestedType)
        .social(social)
        .build();

    reviewRepository.save(review);

    log.info("✔️ 리뷰가 생성되었습니다. ID: {}", review.getId());

    return ReviewResponse.fromEntity(review, true);
  }

  // ✅ 리뷰 1개 조회
  @Transactional(readOnly = true)
  public ReviewResponse getReviewById(Long reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
    Long userId = userService.getCurrentUser().getId();
    return ReviewResponse.fromEntity(review, isMyReview(reviewId, userId));
  }

  // ✅ 리뷰 목록 조회 (페이징)
  @Transactional(readOnly = true)
  public Page<ReviewResponse> getReviewsByFilter(Long filterId, int page, int size) {
    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 필터입니다."));

    PageRequest pageable = PageRequest.of(page, size);
    Page<Review> reviews = reviewRepository.findAllByFilterOrderByCreatedAtDesc(filter, pageable);
    for (Review review : reviews) {
      log.info("🔍 리뷰 ID: {}, 작성자 ID: {}", review.getId(), review.getReviewer().getId());
    }

    Long currentUserId = userService.getCurrentUser().getId();
    // 모든 리뷰에 대해 isMine 플래그 설정
    return reviews.map(review -> ReviewResponse.fromEntity(review, isMyReview(review.getId(), currentUserId)));
  }

  // ✅ 내가 작성한 리뷰 목록 조회 (페이징)
  @Transactional(readOnly = true)
  public Page<MyReviewResponse> getMyReviews(int page, int size) {
    Long currentUserId = userService.getCurrentUser().getId();
    PageRequest pageable = PageRequest.of(page, size);

    Page<Review> reviews = reviewRepository.findAllByReviewerIdOrderByCreatedAtDesc(currentUserId, pageable);

    return reviews.map(review -> {
      Filter filter = review.getFilter();
      boolean usage = filterTransactionRepository.existsByBuyerIdAndFilterId(currentUserId, filter.getId());
      PriceDisplayType type = PriceDisplayType.getType(usage, filter.getPrice());
      return MyReviewResponse.fromEntity(review, filter, type);
    });
  }

  // ✅ 리뷰 미리보기 5개
  @Transactional(readOnly = true)
  public List<ReviewResponse> getReviewPreview(Long filterId) {
    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 필터입니다."));

    List<Review> reviews = reviewRepository.findTop5ByFilterOrderByCreatedAtDesc(filter);

    Long currentUserId = userService.getCurrentUser().getId();
    return reviews.stream()
        .map(review -> ReviewResponse.fromEntity(review, isMyReview(review.getId(), currentUserId)))
        .toList();
  }

  // ✅ 리뷰 삭제
  @Transactional
  public void deleteReview(Long reviewId) {
    Long currentUserId = userService.getCurrentUser().getId();
    // 내 리뷰가 아니면 exception
    if (!isMyReview(reviewId, currentUserId)) {
      throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
    }

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

    reviewRepository.delete(review);
  }

  public boolean isMyReview(Long reviewId, Long userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
    return review.getReviewer().getId().equals(userId);
  }
}
