package com.feelem.server.config.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String token = resolveToken(request); // Request Header에서 JWT 토큰 추출

    try {
      // 토큰 유효성 검사
      if (token != null && jwtTokenProvider.validateToken(token)) {
        // 토큰이 유효할 경우, Authentication 객체를 만들어서 SecurityContext에 저장
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("인증 성공: {}", authentication.getName());
      } else if (token != null) { // 토큰은 있으나 validateToken에서 false를 반환한 경우 (다른 유효하지 않은 토큰)
        log.info("유효하지 않은 JWT 토큰입니다. URI: {}", request.getRequestURI());
        // 여기서도 403 Forbidden 등으로 처리할 수 있습니다.
        // 하지만 ExpiredJwtException을 던지도록 했으므로 이 블록은 만료가 아닌 다른 유효성 검사 실패 케이스입니다.
        // 만료 토큰은 아래 catch 블록에서 처리됩니다.
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
        response.getWriter().write("Invalid Token or Not Supported Token");
        return; // 필터 체인 중단
      }
    } catch (ExpiredJwtException e) { // ⬅️ 여기가 중요! 만료된 토큰 처리
      log.warn("만료된 JWT 토큰입니다. URI: {}", request.getRequestURI());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
      response.getWriter().write("Access Token Expired");
      return; // ⬅️ 필터 체인 중단
    } catch (Exception e) { // 그 외 JWT 관련 예외 처리 (Optional)
      log.error("JWT 토큰 처리 중 오류 발생. URI: {}", request.getRequestURI(), e);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
      response.getWriter().write("JWT Processing Error");
      return; // 필터 체인 중단
    }

    // 모든 필터 체인에서 예외 없이 통과한 경우에만 다음 필터로 진행
    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      // ✅ "Bearer " 뒤에 실제 토큰이 있는지 확인
      if (bearerToken.length() > 7) {
        return bearerToken.substring(7);
      } else {
        log.warn("Authorization header exists but token is empty: '{}'", bearerToken);
        return null;
      }
    }

    if (StringUtils.hasText(bearerToken)) {
      log.warn("Authorization header format invalid: '{}'", bearerToken);
    }

    return null;
  }

}