package com.feelem.server.domain.user;

import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.global.dto.TokenRequestDto;

public interface AuthService {
  TokenInfo reissue(String refreshToken); // refreshToken만 받도록 수정
}