package com.feelem.server.config.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo { // DTO
  private String grantType;
  private String accessToken;
  private String refreshToken;
}