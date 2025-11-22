package com.feelem.server.domain.finance.entity;


public enum FilterTransactionType {
  INIT,
  PURCHASE,   // 포인트를 사용해 구매
  FREE_USE,   // 무료 필터 사용
  REFUND      // 환불
}