package com.feelem.server.domain.user;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
  private Map<String, Object> attributes;
  private String nameAttributeKey;
  private String provider;
  private String providerId;
  private String email;
  private String nickname;

  @Builder
  public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
      String provider, String providerId,
      String email, String nickname) {
    this.attributes = attributes;
    this.nameAttributeKey = nameAttributeKey;
    this.provider = provider;
    this.providerId = providerId;
    this.email = email;
    this.nickname = nickname;
  }

  public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
    if ("google".equals(registrationId)) {
      return ofGoogle(userNameAttributeName, attributes);
    }
    // TODO: naver, kakao 등 추가
    throw new IllegalArgumentException("Unsupported provider: " + registrationId);
  }

  private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");
    String sub = (String) attributes.get("sub"); // Google에서 유저 고유 ID
    return OAuthAttributes.builder()
        .email(email)
        .nickname(name)
        .provider("google")
        .providerId(sub)
        .attributes(attributes)
        .nameAttributeKey(userNameAttributeName)
        .build();
  }

  public User toEntity() {
    return User.builder()
        .email(email != null ? email : UUID.randomUUID() + "@tempuser.com") // ✅ null 방지
        .nickname(nickname)
        .provider(provider)
        .providerId(providerId)
        .role(Role.USER)
        .build();
  }
}
