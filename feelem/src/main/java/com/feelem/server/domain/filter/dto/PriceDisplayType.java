package com.feelem.server.domain.filter.dto;

public enum PriceDisplayType {
  NONE, // 가격표시 X
  PURCHASED, // 구매 완료
  NUMBER; // 가격 표시
  
  public static PriceDisplayType getType(boolean usage, int price) {
    if (usage) {
      return PURCHASED;
    } else {
      if (price <= 0) {
        return NONE;
      } else {
        return NUMBER;
      }
    }
  }
}
