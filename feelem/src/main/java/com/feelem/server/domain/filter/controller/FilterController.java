package com.feelem.server.domain.filter.controller;

import com.feelem.server.domain.filter.dto.FilterCreateRequest;
import com.feelem.server.domain.filter.dto.FilterPriceDto;
import com.feelem.server.domain.filter.dto.FilterListResponse;
import com.feelem.server.domain.filter.dto.FilterResponse;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.service.FilterService;
import com.feelem.server.domain.upload.service.UploadService;
import com.feelem.server.global.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/filters")
public class FilterController {

  private final FilterService filterService;
  private final NotificationService notificationService;
  private final UploadService uploadService;

  // 기존 코드 유지 (필터 생성)
  @PostMapping
  public ResponseEntity<FilterResponse> createFilter(@RequestBody FilterCreateRequest request) {
    Filter filter = filterService.createFilter(request);
    FilterResponse response = filterService.getFilter(filter.getId());

//    log.info("✔️ 필터가 생성되었습니다: {}", response);

    // 태블릿 앱에 프린트 요청 알림 전송
    try {
      var imageUrl = response.getEditedImageUrl();
      notificationService.sendPrintNotification(imageUrl);
    } catch (Exception e) {
      log.error("⚠️ 태블릿 알림 발송 실패: {}", e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
  
  // 전시용
  // 현재 사진으로 s3 저장, 프린트
  @PostMapping("/print")
  public ResponseEntity<String> print(@RequestParam("file") MultipartFile file) {
    String s3ImageUrl;
    try {
      s3ImageUrl = uploadService.uploadPrintImage(file);
    } catch (Exception e) {
      log.error("❌ S3 업로드 실패: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("S3 업로드 실패");
    }

    // 태블릿 앱에 프린트 요청 알림 전송
    try {
      notificationService.sendPrintNotification(s3ImageUrl);
    } catch (Exception e) {
      log.error("⚠️ 태블릿 알림 발송 실패: {}", e.getMessage());
    }

    return ResponseEntity.status(HttpStatus.OK).body(s3ImageUrl);
  }

  // 기존 코드 유지 (필터 상세 조회)
  @GetMapping("/{filterId}")
  public ResponseEntity<FilterResponse> getFilter(@PathVariable Long filterId) {
    FilterResponse response = filterService.getFilter(filterId);

//    log.info("✔️ 필터가 조회되었습니다: filterId={}", filterId);
  return ResponseEntity.ok(response);
  }

  // 기존 코드 유지 (가격 업데이트)
  @PutMapping("/{filterId}/price")
  public ResponseEntity<Void> updatePrice(
      @PathVariable Long filterId,
      @RequestBody FilterPriceDto request
  ) {
    filterService.updatePrice(filterId, request.getPrice());

//    log.info("✔️ 필터 가격이 업데이트되었습니다: filterId={}, price={}", filterId, request.getPrice());

    return ResponseEntity.ok().build();
  }

  // 기존 코드 유지 (필터 삭제)
  @DeleteMapping("/{filterId}")
  public ResponseEntity<Void> deleteFilter(@PathVariable Long filterId) {
    filterService.deleteFilter(filterId);

//    log.info("✔️ 필터가 삭제되었습니다: filterId={}", filterId);

    return ResponseEntity.noContent().build();
  }

  // 1) 현재 필터를 북마크로 설정(on/off)
  @PutMapping("/{filterId}/bookmark")
  public ResponseEntity<Boolean> toggleBookmark(@PathVariable Long filterId) {

    boolean result = filterService.toggleBookmark(filterId);

//    log.info("⭐ 북마크 토글 완료: filterId={}, bookmark={}", filterId, result);

    return ResponseEntity.ok(result);
  }

  // 3) 필터 구매 or 사용
  @PostMapping("/{filterId}/usage")
  public ResponseEntity<Void> useFilter(@PathVariable Long filterId) {

    filterService.useFilter(filterId);

//    log.info("⭐ 필터 사용/구매 완료: filterId={}", filterId);

    return ResponseEntity.noContent().build();
  }
}
