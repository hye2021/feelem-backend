package com.feelem.server.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  // 소셜 로그인으로 가입한 사용자인지 확인하기 위한 메서드
  Optional<User> findByProviderAndProviderId(String provider, String providerId);

  // Refresh Token으로 사용자 찾기
  Optional<User> findByRefreshToken(String refreshToken);
}