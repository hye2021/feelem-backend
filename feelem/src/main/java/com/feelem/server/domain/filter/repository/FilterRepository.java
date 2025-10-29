package com.feelem.server.domain.filter.repository;

import com.feelem.server.domain.filter.entity.Filter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FilterRepository extends JpaRepository<Filter, Long> {
  Optional<Filter> findByIdAndIsDeletedFalse(Long id);

  /**
   * [추가] ID 리스트에 해당하는 필터와 creator를 Eager 로딩(JOIN FETCH)으로 한 번에 조회합니다.
   */
  @Query("SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.id IN :ids AND f.isDeleted = false")
  List<Filter> findFiltersWithCreatorByIdIn(@Param("ids") List<Long> ids);
}
