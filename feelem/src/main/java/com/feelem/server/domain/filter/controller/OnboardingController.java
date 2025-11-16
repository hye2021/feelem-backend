package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.entity.Onboarding;
import com.feelem.server.domain.filter.service.OnboardingService;
import com.feelem.server.domain.filter.entity.Filter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

  private final OnboardingService onboardingService;


  /**
   * ✅ 온보딩 필터 전체 조회
   * - 온보딩 화면에서 선택지로 보여줄 필터 목록
   * - 내부적으로 Onboarding 엔티티에 연결된 Filter 정보 포함
   */
  @GetMapping("/filters")
  public ResponseEntity<List<OnboardingFilterResponse>> getAllOnboardingFilters() {
    List<OnboardingFilterResponse> filters = onboardingService.getAllOnboardingFilters()
        .stream()
        .map(OnboardingFilterResponse::from)
        .collect(Collectors.toList());

    return ResponseEntity.ok(filters);
  }


  @PostMapping("/bookmark")
  public ResponseEntity<String> addOnboardingBookmark(@RequestBody OnboardingRequest request) {
    onboardingService.addOnboardingFilterToBookmark(request.getFilterId());
    return ResponseEntity.ok("Bookmark added successfully");
  }

  public static class OnboardingRequest {
    private Long filterId;
    public Long getFilterId() { return filterId; }
  }

  // ✅ 응답 DTO (GET /filters에서 사용)
  public static class OnboardingFilterResponse {
    private Long onboardingId;
    private Long filterId;
    private String imageUrl;

    public static OnboardingFilterResponse from(Onboarding onboarding) {
      Filter filter = onboarding.getFilter();
      OnboardingFilterResponse dto = new OnboardingFilterResponse();
      dto.onboardingId = onboarding.getId();
      dto.filterId = filter.getId();
      dto.imageUrl = filter.getEditedImageUrl(); // 필터 엔티티에 썸네일 필드가 있다고 가정
      return dto;
    }

    public Long getOnboardingId() { return onboardingId; }
    public Long getFilterId() { return filterId; }
    public String getImageUrl() { return imageUrl; }
  }
}
