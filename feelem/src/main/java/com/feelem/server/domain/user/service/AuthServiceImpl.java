package com.feelem.server.domain.user.service;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.domain.user.entity.Role;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  // ✅ 기존 RefreshToken 재발급
  @Transactional
  @Override
  public TokenInfo reissue(String refreshToken) {
    jwtTokenProvider.validateToken(refreshToken);
    Claims claims = jwtTokenProvider.parseClaims(refreshToken);
    Long userId = Long.parseLong(claims.getSubject());

    User user = userRepository.findByRefreshToken(refreshToken)
        .orElseThrow(() -> new IllegalArgumentException("잘못된 Refresh Token 입니다."));

    if (!user.getId().equals(userId)) {
      throw new SecurityException("토큰의 사용자 정보가 일치하지 않습니다.");
    }

    Collection<? extends GrantedAuthority> authorities =
        Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey()));
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user.getId().toString(), null, authorities);

    TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication, user.getId());
    user.updateRefreshToken(newTokenInfo.getRefreshToken());
    return newTokenInfo;
  }

  // ✅ 안드로이드 구글 로그인
  @Transactional
  @Override
  public TokenInfo loginWithGoogle(String idTokenString) throws Exception {
    log.info("📥 Received Google ID Token: {}", idTokenString);

    // 1️⃣ Google ID Token 검증
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(),
        JacksonFactory.getDefaultInstance()
    )
        .setAudience(Collections.singletonList(
            "27326569433-37asc7ec60uq68sujfe3ooaud6i9puo7.apps.googleusercontent.com" // ✅ Client ID
        ))
        .setIssuer("https://accounts.google.com")
        .build();

    GoogleIdToken idToken = verifier.verify(idTokenString);
    if (idToken == null) {
      log.error("❌ Invalid Google ID Token");
      throw new IllegalArgumentException("Invalid Google ID Token");
    }

    // 2️⃣ 사용자 정보 추출
    GoogleIdToken.Payload payload = idToken.getPayload();
    String email = payload.getEmail();
    String nickname = (String) payload.get("name");
    String providerId = payload.getSubject();
    String provider = "google";

    log.info("✅ Google verified user: email={}, nickname={}, providerId={}", email, nickname, providerId);

    // 3️⃣ 사용자 등록 또는 조회
    User user = userRepository.findByEmail(email)
        .orElseGet(() -> {
          User newUser = User.builder()
              .email(email)
              .nickname(nickname != null ? nickname : "User_" + providerId.substring(0, 6))
              .provider(provider)
              .providerId(providerId)
              .role(Role.USER)
              .build();
          return userRepository.save(newUser);
        });

    // 4️⃣ JWT 발급
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        user.getEmail(),
        null,
        Collections.singletonList(new SimpleGrantedAuthority(user.getRoleKey()))
    );

    TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, user.getId());
    user.updateRefreshToken(tokenInfo.getRefreshToken());
    userRepository.save(user); // ✅ 업데이트 반영

    log.info("🎫 JWT generated for userId {} -> {}", user.getId(), tokenInfo.getAccessToken());
    return tokenInfo;
  }
}
