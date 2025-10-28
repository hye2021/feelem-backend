package com.feelem.server.domain.user;

import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.global.dto.TokenRequestDto;

public interface AuthService {
  // 🔁 토큰 재발급
  TokenInfo reissue(String refreshToken);

  // 🆕 안드로이드 Google 로그인
  TokenInfo loginWithGoogle(String idTokenString) throws Exception;
}