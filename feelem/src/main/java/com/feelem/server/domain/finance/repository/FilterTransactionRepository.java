package com.feelem.server.domain.finance.repository;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.finance.entity.FilterTransaction;
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

}