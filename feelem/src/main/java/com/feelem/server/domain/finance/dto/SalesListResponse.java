package com.feelem.server.domain.finance.dto;

import com.feelem.server.domain.filter.entity.Filter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalesListResponse {
  // 필터 ID
  private Long filterId;
  // 필터 이름
  private String filterName;
  // 필터 이미지 url
  private String filterImageUrl;
  // 필터 등록일
  private String filterCreatedAt;
  // 가격
  private int price;
  // 판매 수량
  private Long salesCount;
  // 북마크 수
  private Long saveCount;
  
  public static SalesListResponse from(Filter filter) {
    return SalesListResponse.builder()
        .filterId(filter.getId())
        .filterName(filter.getName())
        .filterImageUrl(filter.getEditedImageUrl())
        .filterCreatedAt(filter.getCreatedAt().toString())
        .price(filter.getPrice())
        .saveCount(filter.getSaveCount())
        .salesCount(filter.getPurchaseCount())
        .build();
  }
}
