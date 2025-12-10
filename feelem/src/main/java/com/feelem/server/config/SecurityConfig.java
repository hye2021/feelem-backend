package com.feelem.server.config;

import com.feelem.server.config.jwt.JwtAuthenticationFilter;
import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.domain.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
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
            .anyRequest().permitAll() // 모든 요청 허용
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
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/auth/google",
                "/auth/reissue",
                "/api/v1/admin/**",
                "/error"
            ).permitAll()
            .anyRequest().authenticated()
        )
        // formLogin, httpBasic, oauth2Login 완전히 비활성화
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .oauth2Login(AbstractHttpConfigurer::disable)  // 이 줄이 가장 중요

        // CORS 허용
        .cors(cors -> cors.configurationSource(request -> {
          var corsConfig = new org.springframework.web.cors.CorsConfiguration();
          corsConfig.setAllowedOrigins(java.util.List.of(
              "http://13.124.105.243",    // EC2
              "http://10.0.2.2:8080",     // 에뮬레이터
              "http://localhost:8080"     // 로컬 테스트
          ));
          corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
          corsConfig.setAllowedHeaders(java.util.List.of("*"));
          corsConfig.setExposedHeaders(java.util.List.of("Authorization"));
          corsConfig.setAllowCredentials(true);
          return corsConfig;
        }));

    // JWT 인증 필터 등록
    http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
