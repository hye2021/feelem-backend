package com.feelem.server.domain.finance.repository;

import com.feelem.server.domain.finance.entity.CashTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {
}
