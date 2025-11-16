package com.feelem.server.domain.user.repository;

import com.feelem.server.domain.user.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {

  Optional<Point> findByUserId(Long userId);
}
