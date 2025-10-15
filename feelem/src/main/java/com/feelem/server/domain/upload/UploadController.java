package com.feelem.server.web;

import com.feelem.server.domain.upload.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads")
public class UploadController {

  private final UploadService uploadService;

  @PostMapping("/stickers")
  public ResponseEntity<String> uploadSticker(@RequestParam("file") MultipartFile file) throws Exception {
    String url = uploadService.uploadSticker(file);
    return ResponseEntity.ok(url);
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(@RequestParam String url) {
    uploadService.delete(url);
    return ResponseEntity.ok().build();
  }
}
