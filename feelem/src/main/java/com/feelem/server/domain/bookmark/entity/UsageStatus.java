package com.feelem.server.domain.bookmark.entity;

public enum UsageStatus {
  PURCHASED,   // 구매 완료
  USED,        // 실제 사용 완료
  CANCELLED,   // 취소됨
  REFUNDED     // 환불 완료
}
