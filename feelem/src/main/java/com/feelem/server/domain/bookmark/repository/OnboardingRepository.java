package com.feelem.server.domain.bookmark.repository;

import com.feelem.server.domain.bookmark.entity.Onboarding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {
}
