package com.feelem.server.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nickname;

  // OAuth2 제공자 (google, kakao 등)
  @Column(nullable = false)
  private String provider;

  // OAuth2 제공자로부터 받은 고유 ID
  @Column(name = "provider_id", nullable = false)
  private String providerId;

  @Enumerated(EnumType.STRING) // Enum 이름을 DB에 문자열로 저장
  @Column(nullable = false)
  private Role role;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder
  public User(String nickname, String provider, String providerId, Role role) {
    this.nickname = nickname;
    this.provider = provider;
    this.providerId = providerId;
    this.role = role;
  }

  // OAuth2 사용자 정보 업데이트를 위한 메서드
  public User update(String nickname) {
    this.nickname = nickname;
    return this;
  }

  public String getRoleKey() {
    return this.role.getKey();
  }
}
