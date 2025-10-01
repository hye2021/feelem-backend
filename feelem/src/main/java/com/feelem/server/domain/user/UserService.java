package com.feelem.server.domain.user;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional(readOnly = true)
  public User findById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
  }

  @Transactional
  public TokenInfo reissueToken(String refreshToken) {
    // 1. Refresh Token 유효성 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new IllegalArgumentException("유효하지 않은 Refresh Token 입니다.");
    }

    // 2. DB에 저장된 Refresh Token과 일치하는 사용자를 찾는다.
    User user = userRepository.findByRefreshToken(refreshToken)
        .orElseThrow(() -> new IllegalArgumentException("Refresh Token에 해당하는 사용자가 없습니다."));

    // 3. ⬇️ DB에서 찾은 사용자 정보로 새로운 Authentication 객체를 생성한다.
    Collection<? extends GrantedAuthority> authorities =
        Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey()));

    // 사용자 ID를 principal로 사용하여 Authentication 객체 생성
    Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId().toString(), null, authorities);

    // 4. 새로운 Access Token과 Refresh Token을 생성한다 (Refresh Token Rotation).
    TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication);

    // 5. DB에 새로운 Refresh Token으로 업데이트한다.
    user.updateRefreshToken(newTokenInfo.getRefreshToken());

    return newTokenInfo;
  }
}