package com.feelem.server.domain.filter.repository;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.entity.FilterTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FilterTagRepository extends JpaRepository<FilterTag, Long> {

  @Query("""
        SELECT ft.filter 
        FROM FilterTag ft
        JOIN ft.tag t
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
  Page<Filter> findFiltersByTagName(@Param("keyword") String keyword, Pageable pageable);
}
