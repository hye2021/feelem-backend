package com.feelem.server.domain.user.service;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

  public User getCurrentUser() {
    log.info("✔️ 현재 인증된 사용자를 SecurityContext에서 조회합니다.");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // ✅ OAuth 로그인한 유저는 SecurityContext에서 email 기반으로 가져오기
    if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
      throw new RuntimeException("로그인이 필요합니다.");
    }

    String name = authentication.getName();
    log.info("✔️ 요청 하는 user의 authentication name: {}", name);

    // ✅ 숫자인 경우 ID로 조회
    try {
      Long userId = Long.parseLong(name);
      return userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
    } catch (NumberFormatException e) {
      // ✅ 이메일일 경우 기존 방식 유지
      return userRepository.findByEmail(name)
          .orElseThrow(() -> new RuntimeException("User not found"));
    }
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
    TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication, user.getId());

    // 5. DB에 새로운 Refresh Token으로 업데이트한다.
    user.updateRefreshToken(newTokenInfo.getRefreshToken());

    return newTokenInfo;
  }

  // 닉네임 설정 혹은 변경
  @Transactional
  public User updateNickname(String newNickname) {
    User currentUser = getCurrentUser();
    Long userId = currentUser.getId();
    User user = findById(userId);
    user.update(newNickname);
    return user;
  }

  // 가입된 회원인지 확인: 닉네임이 User_로 시작하는지 확인
  @Transactional(readOnly = true)
  public boolean isRegisteredUser() {
    User currentUser = getCurrentUser();
    Long userId = currentUser.getId();
    User user = findById(userId);
    return !user.getNickname().startsWith("User_");
  }
}