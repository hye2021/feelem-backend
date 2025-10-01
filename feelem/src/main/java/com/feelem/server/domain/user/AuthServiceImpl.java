package com.feelem.server.domain.user;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException; // 만료 예외 추가
import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 필요시

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService { // 또는 UserService

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public TokenInfo reissue(String refreshToken) {
    // 1. Refresh Token 유효성 검증
    jwtTokenProvider.validateToken(refreshToken); // ⬅️ 만료 시 여기서 예외 발생

    // 2. Refresh Token에서 사용자 ID (subject) 추출
    Claims claims = jwtTokenProvider.parseClaims(refreshToken);
    Long userId = Long.parseLong(claims.getSubject());

    // 3. DB에 저장된 Refresh Token과 일치하는 사용자를 찾는다.
    User user = userRepository.findByRefreshToken(refreshToken)
        .orElseThrow(() -> new IllegalArgumentException("잘못된 Refresh Token 입니다."));

    // 4. 사용자 ID가 일치하는지 한번 더 확인 (보안 강화)
    if (!user.getId().equals(userId)) {
      throw new SecurityException("토큰의 사용자 정보가 일치하지 않습니다.");
    }

    // 5. 새로운 Authentication 객체를 생성한다. (DB에서 찾은 user 정보 기반)
    Collection<? extends GrantedAuthority> authorities =
        Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey()));
    Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId().toString(), null, authorities);

    // 6. 새로운 Access Token과 Refresh Token을 생성한다 (Refresh Token Rotation).
    TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication, user.getId());

    // 7. DB에 새로운 Refresh Token으로 업데이트한다.
    user.updateRefreshToken(newTokenInfo.getRefreshToken());

    return newTokenInfo;
  }
}
