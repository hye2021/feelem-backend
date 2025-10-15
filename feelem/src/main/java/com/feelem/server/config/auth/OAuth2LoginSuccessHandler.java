package com.feelem.server.config.auth;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.domain.user.User;
import com.feelem.server.domain.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  // todo: 프론트엔드(안드로이드 앱)의 로그인 성공 리다이렉트 URI (예: myapp://oauth2/callback)
  private final String REDIRECT_URI = "/login/success"; // ⬅️ ⭐ 이 URI를 앱의 스킴(Scheme) 및 호스트에 맞게 변경해야 합니다!
  // 안드로이드 앱의 AndroidManifest.xml에 <intent-filter>를 설정하여 특정 스킴(예: feelem://oauth2/callback)으로 오는 요청을 앱이 처리하도록 해야 합니다.
  // (예: feelem://oauth2/callback)

  @Override
  @Transactional
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    log.info("OAuth2 Login Success Handler 진입");

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    Long userId = oAuth2User.getAttribute("user_id");

    // 1. JWT 토큰 생성
    TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication, userId);
    log.info("Generated Access Token: {}", tokenInfo.getAccessToken());
    log.info("Generated Refresh Token: {}", tokenInfo.getRefreshToken());

    // 2. ⬇️ DB에 Refresh Token 저장 (핵심)
    String provider = oAuth2User.getAttribute("provider");
    String providerId = oAuth2User.getAttribute("providerId");

    User user = userRepository.findByProviderAndProviderId(provider, providerId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    user.updateRefreshToken(tokenInfo.getRefreshToken());
    // userRepository.save(user); // @Transactional 어노테이션의 '더티 체킹'으로 인해 save 호출 없이도 DB에 업데이트 됩니다.

//    // 3. JWT 토큰을 포함하여 클라이언트로 리다이렉트
//    String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
//        .queryParam("accessToken", tokenInfo.getAccessToken())
//        .queryParam("refreshToken", tokenInfo.getRefreshToken())
//        .build().toUriString();

    getRedirectStrategy().sendRedirect(request, response, REDIRECT_URI);
  }
}