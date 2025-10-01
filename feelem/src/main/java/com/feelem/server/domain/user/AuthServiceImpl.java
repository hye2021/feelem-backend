package com.feelem.server.domain.user;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import io.jsonwebtoken.ExpiredJwtException; // 만료 예외 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 필요시

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService { // 또는 AuthService 자체에 구현

  private final JwtTokenProvider jwtTokenProvider;
  // private final RefreshTokenRepository refreshTokenRepository; // Refresh Token을 DB에 저장한다면 필요

  // Refresh Token을 검증하고 새로운 토큰 쌍을 발급
  @Override
  public TokenInfo reissue(String refreshToken) {
    // 1. Refresh Token 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
    }

    // 2. Refresh Token에서 Authentication 정보 가져오기
    // Refresh Token은 subject(사용자 이름)만 가지고 있을 가능성이 높습니다.
    // Access Token처럼 권한 정보를 담지 않았다면,
    // Refresh Token에서 사용자 ID를 추출하여 DB에서 사용자 정보를 조회하거나,
    // 혹은 기존 인증 객체를 활용하는 방식 등 다양한 방법이 있습니다.
    // 여기서는 JwtTokenProvider의 getAuthentication이 Refresh Token도 처리한다고 가정합니다.
    // 주의: Refresh Token은 Access Token과 다른 Claims를 가질 수 있으므로,
    //      jwtTokenProvider.getAuthentication 메서드를 Refresh Token에 맞게 수정해야 할 수 있습니다.
    //      만약 Refresh Token에 권한 정보가 없다면, DB에서 사용자 권한을 조회하는 로직이 필요합니다.
    Authentication authentication;
    try {
      authentication = jwtTokenProvider.getAuthentication(refreshToken);
    } catch (ExpiredJwtException e) {
      throw new RuntimeException("Refresh Token이 만료되었습니다.");
    } catch (Exception e) {
      throw new RuntimeException("유효하지 않은 Refresh Token입니다.", e);
    }


    // (옵션) DB에 저장된 Refresh Token과 일치하는지 확인하는 로직 (Refresh Token 탈취 방지)
    // RefreshToken storedRefreshToken = refreshTokenRepository.findByKey(authentication.getName())
    //     .orElseThrow(() -> new RuntimeException("로그아웃된 사용자입니다."));
    // if (!storedRefreshToken.getValue().equals(refreshToken)) {
    //     throw new RuntimeException("Refresh Token 정보가 일치하지 않습니다.");
    // }

    // 3. 새로운 토큰 생성
    TokenInfo newTokens = jwtTokenProvider.generateToken(authentication);

    // (옵션) DB에 새로운 Refresh Token 저장
    // storedRefreshToken.updateValue(newTokens.getRefreshToken());
    // refreshTokenRepository.save(storedRefreshToken);

    return newTokens;
  }
}
