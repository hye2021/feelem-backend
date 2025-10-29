package com.feelem.server.web;

import com.feelem.server.domain.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads")
public class UploadController {

  private final UploadService uploadService;

  @PostMapping("/stickers")
  public ResponseEntity<String> uploadSticker(@RequestParam("file") MultipartFile file) throws Exception {
    String url = uploadService.uploadSticker(file);

    log.info("✔️ 스티커가 업로드되었습니다: {}", url);

    return ResponseEntity.ok(url);
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(@RequestParam String url) {
    uploadService.delete(url);

    log.info("✔️ 파일이 삭제되었습니다: {}", url);

    return ResponseEntity.ok().build();
  }
}
