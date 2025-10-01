package com.feelem.server.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
  private final Map<String, Object> attributes;
  private final String nameAttributeKey;
  private final String nickname;
  private final String provider;
  private final String providerId;

  @Builder
  public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String nickname, String provider, String providerId) {
    this.attributes = attributes;
    this.nameAttributeKey = nameAttributeKey;
    this.nickname = nickname;
    this.provider = provider;
    this.providerId = providerId;
  }

  // 플랫폼별로 다른 사용자 정보 형식을 통일
  public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
    // (나중에 Kakao, Naver 등 추가 시 여기에 분기 로직 추가)
    return ofGoogle(userNameAttributeName, attributes);
  }

  private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
    return OAuthAttributes.builder()
        .nickname((String) attributes.get("name"))
        .provider("google")
        .providerId((String) attributes.get("sub"))
        .attributes(attributes)
        .nameAttributeKey(userNameAttributeName)
        .build();
  }

  // User 엔티티 생성
  public User toEntity() {
    return User.builder()
        .nickname(nickname)
        .provider(provider)
        .providerId(providerId)
        .role(Role.USER) // 기본 권한
        .build();
  }
}