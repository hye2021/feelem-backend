package com.feelem.server.domain.review.repository;

import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.filter.entity.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  Page<Review> findAllByFilterOrderByCreatedAtDesc(Filter filter, Pageable pageable);

  List<Review> findTop5ByFilterOrderByCreatedAtDesc(Filter filter);

  // User가 작성한 리뷰 최신순 조회
  @Query(value = "SELECT r FROM Review r " +
      "JOIN FETCH r.filter " +       // 필터 정보는 무조건 있으므로 JOIN FETCH
      "JOIN FETCH r.reviewer " +     // 작성자 정보도 JOIN FETCH (이미 ID를 알지만 안전하게)
      "LEFT JOIN FETCH r.social " +  // 소셜은 없을 수도 있으므로 LEFT JOIN FETCH
      "WHERE r.reviewer.id = :reviewerId " +
      "ORDER BY r.createdAt DESC",
      countQuery = "SELECT count(r) FROM Review r WHERE r.reviewer.id = :reviewerId")
  Page<Review> findAllByReviewerIdOrderByCreatedAtDesc(@Param("reviewerId") Long reviewerId, Pageable pageable);
}

