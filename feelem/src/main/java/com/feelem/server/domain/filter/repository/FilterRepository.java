package com.feelem.server.domain.filter.repository;

import com.feelem.server.domain.filter.entity.Filter;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FilterRepository extends JpaRepository<Filter, Long> {

  Optional<Filter> findByIdAndIsDeletedFalse(Long id);

  /**
   * [기존] ID 리스트 조회 (Fetch Join 유지)
   */
  @Query("SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.id IN :ids AND f.isDeleted = false")
  List<Filter> findFiltersWithCreatorByIdIn(@Param("ids") List<Long> ids);

  // -----------------------------------------------------------------------
  // ✅ [수정 1] 최신순 조회 (N+1 해결)
  // JOIN FETCH f.creator: 필터를 가져올 때 작성자 정보도 한 번에 가져옵니다.
  // -----------------------------------------------------------------------
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.isDeleted = false ORDER BY f.createdAt DESC",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.isDeleted = false")
  Page<Filter> findAllByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

  // -----------------------------------------------------------------------
  // ✅ [수정 2] 인기순 조회 (N+1 해결)
  // 마찬가지로 작성자를 Fetch Join 합니다.
  // -----------------------------------------------------------------------
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.isDeleted = false ORDER BY f.saveCount DESC",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.isDeleted = false")
  Page<Filter> findAllByIsDeletedFalseOrderBySaveCountDesc(Pageable pageable);
}