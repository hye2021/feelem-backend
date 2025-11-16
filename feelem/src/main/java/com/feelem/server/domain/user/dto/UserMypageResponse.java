package com.feelem.server.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserMypageResponse {
  private Long userId;
  private String nickname;
  private int pointAmount;
}
