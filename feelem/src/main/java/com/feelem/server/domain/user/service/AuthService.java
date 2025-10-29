package com.feelem.server.domain.user.service;

import com.feelem.server.config.jwt.TokenInfo;

public interface AuthService {
  // 🔁 토큰 재발급
  TokenInfo reissue(String refreshToken);

  // 🆕 안드로이드 Google 로그인
  TokenInfo loginWithGoogle(String idTokenString) throws Exception;
}