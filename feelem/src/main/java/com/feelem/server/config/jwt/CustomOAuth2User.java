package com.feelem.server.config.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

// JWT 인증 시 임시로 사용할 OAuth2User 구현체
public class CustomOAuth2User implements OAuth2User {
  private final String userId;
  private final Collection<? extends GrantedAuthority> authorities;

  public CustomOAuth2User(String userId, Collection<? extends GrantedAuthority> authorities) {
    this.userId = userId;
    this.authorities = authorities;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return null;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getName() {
    return userId;
  }
}