package com.feelem.server.domain.finance.service;

import com.feelem.server.domain.finance.entity.CashTransaction;
import com.feelem.server.domain.finance.entity.CashTransactionType;
import com.feelem.server.domain.finance.repository.CashTransactionRepository;
import com.feelem.server.domain.user.entity.Point;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.PointRepository;
import com.feelem.server.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

  private final UserService userService;
  private final PointRepository pointRepository;
  private final CashTransactionRepository cashTransactionRepository;

  /** 포인트 충전 (현금 → 포인트 환산 포함 + CashTransaction 기록) */
  public int chargePoint(int cash) {

    User user = userService.getCurrentUser();

    Point point = pointRepository.findByUserId(user.getId())
        .orElseThrow(() -> new EntityNotFoundException("포인트 정보가 없습니다."));

    // 현금 → 포인트 환산 (예: 1000 → 100p)
    int convertedPoint = cash / 10;

    // 새로운 잔여 포인트
    int newBalance = point.getAmount() + convertedPoint;

    // 포인트 업데이트
    point.setAmount(newBalance);

    // 거래 로그 기록 (CashTransaction)
    CashTransaction tx = CashTransaction.builder()
        .user(user)
        .type(CashTransactionType.CHARGE) // 💡 고정
        .cash(cash)                       // 실제 충전 현금
        .point(convertedPoint)            // 현금 → 포인트 환산 값
        .afterPoint(newBalance)           // 충전 후 보유 포인트
        .description("이벤트 포인트 지급")         // 필요 시 변경 가능
        .build();

    cashTransactionRepository.save(tx);

    return newBalance;
  }
}

