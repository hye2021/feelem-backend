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
   * ID 리스트 조회 (Fetch Join 유지)
   */
  @Query("SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.id IN :ids AND f.isDeleted = false")
  List<Filter> findFiltersWithCreatorByIdIn(@Param("ids") List<Long> ids);

  /** 최신순 조회 (N+1 해결)
    * JOIN FETCH f.creator: 필터를 가져올 때 작성자 정보도 한 번에 가져옵니다.
    */
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.isDeleted = false ORDER BY f.createdAt DESC",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.isDeleted = false")
  Page<Filter> findAllByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

   /** 인기순 조회 (N+1 해결)
     * 마찬가지로 작성자를 Fetch Join 합니다.
     */
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.isDeleted = false ORDER BY f.saveCount DESC",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.isDeleted = false")
  Page<Filter> findAllByIsDeletedFalseOrderBySaveCountDesc(Pageable pageable);

   /** 랜덤 조회 (N+1 해결)*/
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.isDeleted = false ORDER BY function('RAND')",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.isDeleted = false")
  Page<Filter> findAllByIsDeletedFalseOrderByRandom(Pageable pageable);

  /** 특정 User가 제작한 필터 목록 조회*/
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.creator.id = :creatorId AND f.isDeleted = false ORDER BY f.createdAt DESC",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.creator.id = :creatorId AND f.isDeleted = false")
  Page<Filter> findAllByCreatorIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("creatorId") Long creatorId, Pageable pageable);

  /** 태그 리스트를 모두 포함하는 필터 조회
   * Logic: 필터별로 매칭된 태그 개수를 세어서, 입력된 태그 개수와 같은지 확인
   */
  @Query("SELECT f FROM Filter f " +
      "JOIN f.filterTags ft " +
      "JOIN ft.tag t " +
      "WHERE t.name IN :tags " +
      "AND f.isDeleted = false " +
      "GROUP BY f " +
      "HAVING COUNT(DISTINCT t.name) = :tagCount")
  List<Filter> findByTagsContainingAll(
      @Param("tags") List<String> tags,
      @Param("tagCount") Long tagCount
  );
}