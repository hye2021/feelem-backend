package com.feelem.server.domain.filter.service;

import com.feelem.server.domain.filter.entity.Onboarding;
import com.feelem.server.domain.filter.repository.OnboardingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

  private final OnboardingRepository onboardingRepository;
  private final FilterService filterService;  // 🔄 북마크 기능이 FilterService로 통합됨

  /** 온보딩 필터 전체 조회 */
  public List<Onboarding> getAllOnboardingFilters() {
    return onboardingRepository.findAll();
  }

  /** 온보딩에서 선택한 필터를 북마크에 추가 */
  @Transactional
  public void addOnboardingFilterToBookmark(Long filterId) {
    filterService.addBookmark(filterId);  // 🔄 BookmarkService → FilterService
  }
}
