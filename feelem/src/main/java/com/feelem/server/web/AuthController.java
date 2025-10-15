// AuthController.java (또는 UserAuthController.java 등)
package com.feelem.server.web;

import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.global.dto.TokenRequestDto;
import com.feelem.server.domain.user.AuthService; // AuthService 인터페이스 또는 구현체
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/login/success")
  public String loginSuccess() {
    return "<h1>로그인 성공 🎉</h1><p>Feel'em OAuth2 로그인 테스트 페이지</p>";
  }

  @PostMapping("/api/v1/auth/reissue") // 재발급 요청 처리
  public ResponseEntity<TokenInfo> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
    TokenInfo tokenInfo = authService.reissue(tokenRequestDto.getRefreshToken());
    return ResponseEntity.ok(tokenInfo);
  }
}