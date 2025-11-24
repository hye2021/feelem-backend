package com.feelem.server.domain.review.repository;

import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.filter.entity.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  Page<Review> findAllByFilterOrderByCreatedAtDesc(Filter filter, Pageable pageable);

  List<Review> findTop5ByFilterOrderByCreatedAtDesc(Filter filter);

  // User가 작성한 리뷰 최신순 조회
  Page<Review> findAllByReviewerIdOrderByCreatedAtDesc(Long reviewerId, Pageable pageable);
}
