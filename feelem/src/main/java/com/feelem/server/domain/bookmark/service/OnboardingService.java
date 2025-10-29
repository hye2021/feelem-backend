package com.feelem.server.domain.bookmark.service;

import com.feelem.server.domain.bookmark.entity.Onboarding;
import com.feelem.server.domain.bookmark.repository.OnboardingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

  private final OnboardingRepository onboardingRepository;
  private final BookmarkService bookmarkService;

  // 온보딩 필터 전체 조회
  public List<Onboarding> getAllOnboardingFilters() {
    return onboardingRepository.findAll();
  }

  // 온보딩에서 선택 → 북마크 추가
  @Transactional
  public void addOnboardingFilterToBookmark(Long filterId) {
    bookmarkService.addBookmark(filterId);
  }
}
