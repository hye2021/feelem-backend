package com.feelem.server.config;

import com.feelem.server.config.auth.OAuth2LoginSuccessHandler;
import com.feelem.server.config.jwt.JwtAuthenticationFilter;
import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.domain.user.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(CsrfConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 대신 JWT 사용 예정
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/v1/auth/reissue").permitAll()
            .requestMatchers("/api/v1/uploads/**").permitAll() // todo: test
            .requestMatchers("/api/v1/**").authenticated() // /api/v1/** 경로는 인증 필요
            .anyRequest().permitAll() // 그 외 경로는 모두 허용 (로그인 페이지 등)
        )
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService) // ⬅️ 핵심: 로그인 성공 후 사용자 정보를 처리할 서비스
            )
            .successHandler(oAuth2LoginSuccessHandler)
        );

    // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
    http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);


    return http.build();
  }
}