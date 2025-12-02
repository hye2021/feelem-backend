package com.feelem.server.domain.filter.repository;

import com.feelem.server.domain.filter.entity.Filter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FilterRepository extends JpaRepository<Filter, Long> {

  List<Filter> findAllByIsDeletedFalse();

  Optional<Filter> findByIdAndIsDeletedFalse(Long id);

  /**
   * ID 리스트 조회 (Fetch Join 유지)
   */
  @Query("SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.id IN :ids AND f.isDeleted = false")
  List<Filter> findFiltersWithCreatorByIdIn(@Param("ids") List<Long> ids);

  /**
   * 최신순 조회 (N+1 해결)
   * JOIN FETCH f.creator: 필터를 가져올 때 작성자 정보도 한 번에 가져옵니다.
   */
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.isDeleted = false ORDER BY f.createdAt DESC",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.isDeleted = false")
  Page<Filter> findAllByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

  /**
   * 인기순 조회 (N+1 해결)
   * saveCount 기준으로 정렬합니다.
   */
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.isDeleted = false ORDER BY f.saveCount DESC",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.isDeleted = false")
  Page<Filter> findAllByIsDeletedFalseOrderBySaveCountDesc(Pageable pageable);

  /**
   * 랜덤 조회 (N+1 해결)
   */
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.isDeleted = false ORDER BY function('RAND')",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.isDeleted = false")
  Page<Filter> findAllByIsDeletedFalseOrderByRandom(Pageable pageable);

  /**
   * 정렬 조건이 메서드 이름에 포함되지 않은 버전
   */
  Page<Filter> findAllByCreatorIdAndIsDeletedFalse(Long creatorId, Pageable pageable);

  /**
   * 특정 User가 제작한 필터 목록 조회
   */
  @Query(value = "SELECT f FROM Filter f JOIN FETCH f.creator WHERE f.creator.id = :creatorId AND f.isDeleted = false ORDER BY f.createdAt DESC",
      countQuery = "SELECT count(f) FROM Filter f WHERE f.creator.id = :creatorId AND f.isDeleted = false")
  Page<Filter> findAllByCreatorIdAndIsDeletedFalseOrderByCreatedAtDesc(@Param("creatorId") Long creatorId, Pageable pageable);

  /**
   * 태그 리스트를 모두 포함하는 필터 조회
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

  // ==========================================
  //  동시성 처리를 위한 직접 Update 쿼리 (추가됨)
  // ==========================================

  /**
   * 저장(북마크) 수 1 증가
   * clearAutomatically = true: 쿼리 수행 후 영속성 컨텍스트를 비워 데이터 불일치 방지
   */
  @Modifying(clearAutomatically = true)
  @Query("UPDATE Filter f SET f.saveCount = f.saveCount + 1 WHERE f.id = :id AND f.isDeleted = false")
  void increaseSaveCount(@Param("id") Long id);

  /**
   * 저장(북마크) 수 1 감소
   * 0 이하로 내려가지 않도록 조건 추가 (AND f.saveCount > 0)
   */
  @Modifying(clearAutomatically = true)
  @Query("UPDATE Filter f SET f.saveCount = f.saveCount - 1 WHERE f.id = :id AND f.saveCount > 0 AND f.isDeleted = false")
  void decreaseSaveCount(@Param("id") Long id);

  /**
   * 사용 수 1 증가
   */
  @Modifying(clearAutomatically = true)
  @Query("UPDATE Filter f SET f.useCount = f.useCount + 1 WHERE f.id = :id AND f.isDeleted = false")
  void increaseUseCount(@Param("id") Long id);

  /**
   * 구매 수 1 증가
   */
  @Modifying(clearAutomatically = true)
  @Query("UPDATE Filter f SET f.purchaseCount = f.purchaseCount + 1, f.totalSalesAmount = f.totalSalesAmount + :price WHERE f.id = :id AND f.isDeleted = false")
  void increasePurchaseCountAndAmount(@Param("id") Long id, @Param("price") int price);

  /**
   *제목(이름)에 검색어가 포함된 필터 찾기 (최신순 정렬)
   */
//  List<Filter> findByNameContainingAndIsDeletedFalseOrderByCreatedAtDesc(String name);

  /**
   *제목(이름)에 검색어가 포함된 필터 찾기 (최신순 정렬)
   * JPQL 버전.. -> 이스케이프 처리 등 커스텀 필요시 사용
   */
  @Query("SELECT f FROM Filter f WHERE f.name LIKE %:name% AND f.isDeleted = false ORDER BY f.createdAt DESC")
  List<Filter> findByNameSearch(@Param("name") String name);
}