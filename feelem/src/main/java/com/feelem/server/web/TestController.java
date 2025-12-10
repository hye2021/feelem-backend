package com.feelem.server.web;

import com.feelem.server.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

  private final UserService userService;

  @Operation(summary = "인증된 사용자 정보 확인")
  @GetMapping("/me")
  public ResponseEntity<String> getMyInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
    String userId = oAuth2User.getName();
    return ResponseEntity.ok("인증 성공! 당신의 ID는: " + userId);
  }
}


@Getter
@NoArgsConstructor
class TokenReissueRequestDto {
  private String refreshToken;
}