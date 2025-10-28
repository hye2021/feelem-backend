package com.feelem.server.web;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.domain.user.AuthService;
import com.feelem.server.domain.user.Role;
import com.feelem.server.domain.user.User;
import com.feelem.server.domain.user.UserRepository;
import com.feelem.server.global.dto.TokenRequestDto;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  // ✅ 웹 로그인 성공 테스트 페이지 (유지)
  @GetMapping("/login/success")
  public String loginSuccess() {
    return "<h1>로그인 성공 🎉</h1><p>Feel'em OAuth2 로그인 테스트 페이지</p>";
  }

  // ✅ 토큰 재발급
  @PostMapping("/api/v1/auth/reissue")
  public ResponseEntity<TokenInfo> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
    TokenInfo tokenInfo = authService.reissue(tokenRequestDto.getRefreshToken());
    return ResponseEntity.ok(tokenInfo);
  }

  // ✅ 안드로이드 Google 로그인
  @PostMapping("/api/v1/auth/google")
  @Transactional
  public ResponseEntity<TokenInfo> googleLogin(@RequestBody Map<String, String> body) throws Exception {
    String idTokenString = body.get("idToken");
    log.info("📥 Received Google ID Token: {}", idTokenString);

    // 1️⃣ Google ID Token 검증기
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(),
        JacksonFactory.getDefaultInstance()
    )
        .setAudience(Collections.singletonList("27326569433-eokh5p59q9om644u76ndmbuf9sjuvq4a.apps.googleusercontent.com")) // 👈 Android용 OAuth Client ID 입력
        .build();

    // 2️⃣ 검증
    GoogleIdToken idToken = verifier.verify(idTokenString);
    if (idToken == null) {
      log.error("❌ Invalid Google ID Token");
      throw new IllegalArgumentException("Invalid Google ID Token");
    }

    // 3️⃣ 사용자 정보 추출
    GoogleIdToken.Payload payload = idToken.getPayload();
    String email = payload.getEmail();
    String nickname = (String) payload.get("name");
    String providerId = payload.getSubject();
    String provider = "google";

    log.info("✅ Google verified user: email={}, nickname={}, providerId={}", email, nickname, providerId);

    // 4️⃣ DB에 존재하지 않으면 새로 등록
    User user = userRepository.findByEmail(email)
        .orElseGet(() -> userRepository.save(User.builder()
            .email(email)
            .nickname(nickname != null ? nickname : "User_" + providerId.substring(0, 6))
            .provider(provider)
            .providerId(providerId)
            .role(Role.USER)
            .build()));

    // 5️⃣ Authentication 객체 생성 (JwtTokenProvider 요구사항 맞추기)
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        user.getEmail(), // principal
        null,
        Collections.singletonList(new SimpleGrantedAuthority(user.getRoleKey())) // ROLE_USER
    );

    // 6️⃣ JWT 발급
    TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, user.getId());
    user.updateRefreshToken(tokenInfo.getRefreshToken());
    log.info("🎫 JWT generated for userId {} -> {}", user.getId(), tokenInfo.getAccessToken());

    return ResponseEntity.ok(tokenInfo);
  }
}
