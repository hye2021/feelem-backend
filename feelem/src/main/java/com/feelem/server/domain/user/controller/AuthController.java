package com.feelem.server.domain.user.controller;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.domain.user.dto.TokenRequestDto;
import com.feelem.server.domain.user.service.AuthService;
import com.feelem.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    log.info("✅ 웹 로그인 성공 페이지에 접근했습니다.");
    return "<h1>로그인 성공 🎉</h1><p>Feel'em OAuth2 로그인 테스트 페이지</p>";
  }

  // ✅ 토큰 재발급
  @PostMapping("/auth/reissue")
  public ResponseEntity<TokenInfo> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
    TokenInfo tokenInfo = authService.reissue(tokenRequestDto.getRefreshToken());
    return ResponseEntity.ok(tokenInfo);
  }

  // ✅ 안드로이드 Google 로그인
  @PostMapping("/auth/google")
  public ResponseEntity<TokenInfo> googleLogin(@RequestBody Map<String, String> body) throws Exception {
    log.info("✅ 구글 로그인 요청이 들어왔습니다. 요청 바디: {}", body);

    String idToken = body.get("idToken");
    TokenInfo tokenInfo = authService.loginWithGoogle(idToken);

    log.info("✅ 구글 로그인이 성공되었습니다. idToken: {}", idToken);
    log.info("✅ 발급된 JWT 정보: {}", tokenInfo);

    return ResponseEntity.ok(tokenInfo);
  }
}
