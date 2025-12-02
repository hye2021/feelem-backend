package com.feelem.server.domain.finance.repository;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.finance.entity.FilterTransaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterTransactionRepository extends JpaRepository<FilterTransaction, Long> {

  boolean existsByBuyerIdAndFilterId(Long userId, Long filterId);

  @Query("select ft from FilterTransaction ft " +
          "join fetch ft.filter f " +
          "where ft.buyer.id = :userId and f.id = :filterId")
  FilterTransaction findByBuyerIdAndFilterId(Long userId, Long filterId);

  @Query("select ft.filter from FilterTransaction ft where ft.buyer.id = :userId")
  Page<Filter> findUsedOrPurchasedFilters(@Param("userId") Long userId, Pageable pageable);

  // type이 PURCHASE인 경우만
  @Query("select ft from FilterTransaction ft where ft.buyer.id = :userId and ft.type = 'PURCHASE' order by ft.purchasedAt desc")
  Page<FilterTransaction> findUsageHistory(@Param("userId") Long userId, Pageable pageable);

  /**
   * 특정 판매자(Seller)의 특정 기간 내 '구매(PURCHASE)' 내역 조회
   * (종합 판매 수치 & 그래프 계산용)
   */
  @Query("SELECT ft FROM FilterTransaction ft " +
      "WHERE ft.seller.id = :sellerId " +
      "AND ft.type = 'PURCHASE' " +
      "AND ft.purchasedAt BETWEEN :startDate AND :endDate")
  List<FilterTransaction> findAllBySellerIdAndDateRange(
      @Param("sellerId") Long sellerId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate
  );

  /**
   * 특정 필터(Filter)의 특정 기간 내 '구매(PURCHASE)' 내역 조회
   * (개별 필터 그래프 계산용)
   */
  @Query("SELECT ft FROM FilterTransaction ft " +
      "WHERE ft.filter.id = :filterId " +
      "AND ft.type = 'PURCHASE' " +
      "AND ft.purchasedAt BETWEEN :startDate AND :endDate")
  List<FilterTransaction> findAllByFilterIdAndDateRange(
      @Param("filterId") Long filterId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate
  );

}