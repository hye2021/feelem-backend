package com.feelem.server.domain.user.entity;

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

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String provider;

  @Column(name = "provider_id", nullable = false)
  private String providerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "refresh_token")
  private String refreshToken;

  /* 생성자 및 메서드 */

  @Builder
  public User(String nickname, String provider, String providerId, Role role, String email) {
    this.email = email;
    this.nickname = nickname;
    this.provider = provider;
    this.providerId = providerId;
    this.role = role;
  }

  public User update(String nickname) {
    this.nickname = nickname;
    return this;
  }

  public String getRoleKey() {
    return this.role.getKey();
  }

  public void updateRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
