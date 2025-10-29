package com.feelem.server.domain.sticker.controller;

import com.feelem.server.domain.sticker.service.StickerAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stickers/ai")
public class StickerAIController {

  private final StickerAIService stickerAIService;

  /**
   * ✅ 프롬프트로 AI 스티커 생성 (S3 업로드 + DB 저장)
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> generateAISticker(@RequestBody Map<String, String> request)
      throws IOException {
    String prompt = request.get("prompt");
    Map<String, Object> response = stickerAIService.generateDummySticker(prompt);
    return ResponseEntity.ok(response);
  }

  /**
   * ✅ 다시 만들기 (기존 파일 삭제)
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAISticker(@PathVariable Long id) {
    stickerAIService.deleteGeneratedSticker(id);
    return ResponseEntity.noContent().build();
  }
}
