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
  @PostMapping("/api/v1/auth/google")
  public ResponseEntity<TokenInfo> googleLogin(@RequestBody Map<String, String> body) throws Exception {
    String idToken = body.get("idToken");
    TokenInfo tokenInfo = authService.loginWithGoogle(idToken);
    return ResponseEntity.ok(tokenInfo);
  }
}
