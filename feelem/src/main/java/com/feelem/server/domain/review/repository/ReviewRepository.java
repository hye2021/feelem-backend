package com.feelem.server.domain.review.repository;

import com.feelem.server.domain.review.entity.Review;
import com.feelem.server.domain.filter.entity.Filter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  Page<Review> findAllByFilterOrderByCreatedAtDesc(Filter filter, Pageable pageable);
}
