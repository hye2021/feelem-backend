package com.feelem.server.domain.filter;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FilterRepository extends JpaRepository<Filter, Long> {
  Optional<Filter> findByIdAndIsDeletedFalse(Long id);
}
