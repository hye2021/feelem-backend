package com.feelem.server.domain.upload;

import com.feelem.server.domain.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads")
public class UploadController {

  private final UploadService uploadService;

  /**
   * ✅ 스티커 업로드
   */
  @PostMapping("/stickers")
  public ResponseEntity<String> uploadSticker(@RequestParam("file") MultipartFile file) throws Exception {
    String url = uploadService.uploadSticker(file);
    log.info("✔️ 스티커 업로드 완료: {}", url);
    return ResponseEntity.ok(url);
  }

  /**
   * ✅ 스티커 이미지(필터용) 업로드
   */
  @PostMapping("/sticker-images")
  public ResponseEntity<String> uploadStickerImage(@RequestParam("file") MultipartFile file)
      throws Exception {
    String url = uploadService.uploadStickerImage(file);
    log.info("✔️ 스티커 이미지(필터용) 업로드 완료: {}", url);
    return ResponseEntity.ok(url);
  }

  /**
   * ✅ 필터 오리지널 + 프리뷰 이미지를 한 번에 업로드
   */
  @PostMapping("/filters")
  public ResponseEntity<Map<String, String>> uploadFilterImages(
      @RequestParam("original") MultipartFile originalImage,
      @RequestParam("preview") MultipartFile previewImage) throws Exception {

    // 각각 업로드
    String originalUrl = uploadService.uploadFilterOriginal(originalImage);
    String previewUrl = uploadService.uploadFilterPreview(previewImage);

    log.info("✔️ 필터 이미지 업로드 완료 (original: {}, preview: {})", originalUrl, previewUrl);

    // 응답 데이터 구성
    Map<String, String> response = new HashMap<>();
    response.put("originalImageUrl", originalUrl);
    response.put("previewImageUrl", previewUrl);

    return ResponseEntity.ok(response);
  }

  /**
   * ✅ 필터 리뷰 이미지 업로드
   */
  @PostMapping("/filters/reviews")
  public ResponseEntity<String> uploadFilterReview(@RequestParam("file") MultipartFile file) throws Exception {
    String url = uploadService.uploadFilterReview(file);
    log.info("✔️ 필터 리뷰 업로드 완료: {}", url);
    return ResponseEntity.ok(url);
  }

  /**
   * ✅ 파일 삭제
   */
  @DeleteMapping
  public ResponseEntity<Void> delete(@RequestParam String url) {
    uploadService.delete(url);
    log.info("🗑️ 파일 삭제 완료: {}", url);
    return ResponseEntity.ok().build();
  }
}
