package com.feelem.server.domain.finance.service;

import com.feelem.server.domain.finance.dto.CashTransactionListResponse;
import com.feelem.server.domain.finance.dto.FilterTransactionListResponse;
import com.feelem.server.domain.finance.repository.CashTransactionRepository;
import com.feelem.server.domain.finance.repository.FilterTransactionRepository;
import com.feelem.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

  private final CashTransactionRepository cashTransactionRepository;
  private final FilterTransactionRepository filterTransactionRepository;
  private final UserService userService;

  /** 포인트 충전 내역 조회 */
  public Page<CashTransactionListResponse> getChargeHistory(Pageable pageable) {
    Long userId = userService.getCurrentUser().getId();

    return cashTransactionRepository.findChargeHistory(userId, pageable)
        .map(CashTransactionListResponse::from);
  }

  /** 포인트 사용 내역 조회 */
  public Page<FilterTransactionListResponse> getUsageHistory(Pageable pageable) {
    Long userId = userService.getCurrentUser().getId();

    return filterTransactionRepository.findUsageHistory(userId, pageable)
        .map(FilterTransactionListResponse::from);
  }
}
