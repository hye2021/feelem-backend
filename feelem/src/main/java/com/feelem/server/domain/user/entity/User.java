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

  // =================================================================
  // ✅ [추가] 연관관계 매핑 (Cascade 설정)
  // mappedBy = "user": 외래키(FK)는 Social과 Point 테이블에 있다는 뜻
  // cascade = CascadeType.ALL: User가 저장/삭제되면 얘네도 같이 됨
  // =================================================================
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Social social;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Point point;


  // =================================================================
  // ✅ [수정] 생성자에서 하위 엔티티 자동 생성 및 연결
  // =================================================================
  @Builder
  public User(String nickname, String provider, String providerId, Role role, String email) {
    this.email = email;
    this.nickname = nickname;
    this.provider = provider;
    this.providerId = providerId;
    this.role = role;
    this.createdAt = LocalDateTime.now();

    // ★ 핵심 로직: User 생성 시점에 Social과 Point도 같이 만들어서 'this(나)'와 연결함
    this.social = new Social(this, null, null);
    this.point = new Point(this, 0);
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