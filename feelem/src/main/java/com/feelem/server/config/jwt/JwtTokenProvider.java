package com.feelem.server.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

  private final Key key;

  // application.yml에서 secret 값 가져와서 key에 저장
  public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  // 유저 정보를 가지고 AccessToken, RefreshToken을 생성하는 메서드
  public TokenInfo generateToken(Authentication authentication, Long userId) {
    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    long now = (new Date()).getTime();
    Date accessTokenExpiresIn = new Date(now + 60000); // 테스트용 1분

    String accessToken = Jwts.builder()
        .setSubject(userId.toString()) // ⬅️ authentication.getName() 대신 우리 DB의 User ID 사용
        .claim("auth", authorities)
        .setExpiration(accessTokenExpiresIn)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    String refreshToken = Jwts.builder()
        .setSubject(userId.toString()) // ⬅️ Refresh Token에도 User ID 추가
        .setExpiration(new Date(now + 86400000))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    return TokenInfo.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
  public Authentication getAuthentication(String accessToken) { // 이제 accessToken 보다는 token으로 이름 변경하는게 좋을듯
    // 토큰 복호화
    Claims claims = parseClaims(accessToken); // 여기서 accessToken 변수가 refreshToken일 수도 있음

    // 💡 Refresh Token은 'auth' 클레임이 없을 수 있으므로 조건부 처리
    Collection<? extends GrantedAuthority> authorities;
    if (claims.get("auth") != null) { // 'auth' 클레임이 있다면 (Access Token인 경우)
      authorities =
          Arrays.stream(claims.get("auth").toString().split(","))
              .map(SimpleGrantedAuthority::new)
              .collect(Collectors.toList());
    } else { // 'auth' 클레임이 없다면 (Refresh Token인 경우)
      log.warn("토큰에 'auth' 클레임이 없습니다. Refresh Token으로 간주하고 subject만 사용합니다.");
      // 기본 권한을 부여하거나, DB에서 사용자 권한을 조회하는 로직을 추가할 수 있습니다.
      // 여기서는 일단 비어있는 권한 리스트를 사용합니다.
      authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")); // 예시: 기본 권한 부여
      // 혹은 비어있는 리스트: new ArrayList<>();
    }


    // UserDetails 객체를 만들어서 Authentication 리턴
    // 여기서 UserDetails는 Spring Security에서 사용하는 사용자 정보 객체
    // 우리는 OAuth2User를 사용하므로, 이를 UserDetails처럼 활용
    OAuth2User principal = new CustomOAuth2User(claims.getSubject(), authorities);

    // 💡 SecurityContextHolder에 저장될 Authentication 객체는 `principal`, `credentials`, `authorities`를 가집니다.
    // Refresh Token의 경우 credentials는 필요없을 수 있습니다.
    return new UsernamePasswordAuthenticationToken(principal, "", authorities); // credentials는 빈 문자열로 둬도 무방
  }

  // 토큰 정보를 검증하는 메서드
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("Invalid JWT Token", e);
      // 이 경우들은 false를 반환하여 일반적인 유효하지 않은 토큰으로 처리
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT Token", e);
      throw e; // ⬅️ 여기가 핵심! 만료 예외는 다시 던져서 상위에서 catch하도록
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT Token", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT claims string is empty.", e);
    }
    return false; // 다른 예외 발생 시에는 false 반환
  }

  public Claims parseClaims(String accessToken) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }
}