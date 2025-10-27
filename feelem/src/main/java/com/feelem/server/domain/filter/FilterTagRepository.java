package com.feelem.server.domain.filter;

import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FilterTagRepository extends JpaRepository<FilterTag, Long> {

  @Query("""
        SELECT ft.filter 
        FROM FilterTag ft
        JOIN ft.tag t
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
  Page<Filter> findFiltersByTagName(@Param("keyword") String keyword, Pageable pageable);
}
