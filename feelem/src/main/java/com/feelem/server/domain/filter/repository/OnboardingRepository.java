package com.feelem.server.domain.filter.repository;

import com.feelem.server.domain.filter.entity.Onboarding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingRepository extends JpaRepository<Onboarding, Long> {
}
