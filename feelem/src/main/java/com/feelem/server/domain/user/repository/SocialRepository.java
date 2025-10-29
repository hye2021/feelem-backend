package com.feelem.server.domain.user.repository;

import com.feelem.server.domain.user.entity.Social;
import com.feelem.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialRepository extends JpaRepository<Social, Long> {
  Optional<Social> findByUser(User user);
}
