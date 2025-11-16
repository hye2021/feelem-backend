package com.feelem.server.domain.finance.repository;

import com.feelem.server.domain.finance.entity.CashTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {

  @Query("select ct from CashTransaction ct where ct.user.id = :userId order by ct.createdAt desc")
  Page<CashTransaction> findChargeHistory(@Param("userId") Long userId, Pageable pageable);
}

