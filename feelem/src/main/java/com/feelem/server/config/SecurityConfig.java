package com.feelem.server.config;

import com.feelem.server.config.auth.OAuth2LoginSuccessHandler;
import com.feelem.server.config.jwt.JwtAuthenticationFilter;
import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.domain.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
  private final JwtTokenProvider jwtTokenProvider;

  /**
   * ✅ LOCAL 환경 (로컬 테스트용)
   * - 인증 완전 우회
   * - 로그인 없이 Postman 등에서 테스트 가능
   */
  @Bean
  @Profile("local")
  public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll() // ✅ 모든 요청 허용
        );
    return http.build();
  }

  /**
   * ✅ PROD 환경 (운영용)
   * - OAuth2 + JWT 인증 활성화
   */
  @Bean
  @Profile("prod")
  public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(CsrfConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 대신 JWT
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(
                "/login/**",
                "/api/v1/auth/google",
                "/api/v1/auth/reissue"
            ).permitAll()
            .requestMatchers("/api/v1/**").authenticated()
            .anyRequest().permitAll()
        )
        // ✅ 기존 웹용 OAuth2 로그인 완전히 비활성화
        .oauth2Login(oauth2 -> oauth2.disable());

    // ✅ JWT 인증 필터 등록
    http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
