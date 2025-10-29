package com.feelem.server.domain.review.service;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.repository.FilterRepository;
import com.feelem.server.domain.review.dto.ReviewDto;
import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.review.repository.ReviewRepository;
import com.feelem.server.domain.user.entity.Social;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.SocialRepository;
import com.feelem.server.domain.user.repository.UserRepository;
import com.feelem.server.domain.user.service.UserService;
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

  @Transactional
  public Review createReview(ReviewDto.CreateRequest request) {
    Long userId = userService.getCurrentUser().getId();

    User reviewer = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    Filter filter = filterRepository.findById(request.getFilterId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 필터입니다."));

    // ✅ 해당 유저의 소셜 정보 찾기
    Social social = socialRepository.findByUser(reviewer)
        .orElseThrow(() -> new IllegalArgumentException("해당 유저의 소셜 정보가 없습니다."));

    // ✅ 요청한 socialType 유효성 검증
    if (request.getSocialType() != null) {
      boolean valid = switch (request.getSocialType().toLowerCase()) {
        case "instagram" -> social.getInstagramId() != null;
        case "x" -> social.getXId() != null;
        default -> false;
      };
      if (!valid) {
        throw new IllegalArgumentException("유효하지 않은 소셜 종류입니다: " + request.getSocialType());
      }
    }

    Review review = Review.builder()
        .reviewer(reviewer)
        .filter(filter)
        .imageUrl(request.getImageUrl())
        .social(social) // ✅ 전체 소셜 정보 저장
        .build();

    log.info("✔️ 리뷰가 생성되었습니다. 리뷰 ID: {}, 작성자 ID: {}, 필터 ID: {}",
        review.getId(), reviewer.getId(), filter.getId());

    return reviewRepository.save(review);
  }

  @Transactional(readOnly = true)
  public Page<ReviewDto.Response> getReviewsByFilter(Long filterId, int page) {
    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 필터입니다."));

    PageRequest pageable = PageRequest.of(page, 20);
    return reviewRepository.findAllByFilterOrderByCreatedAtDesc(filter, pageable)
        .map(ReviewDto.Response::fromEntity);
  }
}
