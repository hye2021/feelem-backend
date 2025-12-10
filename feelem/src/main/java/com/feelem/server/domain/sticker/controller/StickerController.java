package com.feelem.server.domain.sticker.controller;

import com.feelem.server.domain.sticker.dto.StickerDto;
import com.feelem.server.domain.sticker.entity.Sticker;
import com.feelem.server.domain.sticker.entity.StickerType;
import com.feelem.server.domain.sticker.service.StickerService;
import com.feelem.server.domain.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/stickers")
public class StickerController {

  private final UploadService uploadService;
  private final StickerService stickerService;

  /**
   * 1) 일반/브러시 스티커 업로드 (파일 전송)
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<StickerDto.Response> createSticker(
      @RequestParam("file") MultipartFile file,
      @RequestParam("type") StickerType type) {

    try {
      // 1️이미지 업로드
      String imageUrl = uploadService.uploadSticker(file);
//      log.info("✔️ 스티커 업로드 완료: {}", imageUrl);

      // 2 DB 등록
      Sticker sticker = stickerService.createSticker(type, imageUrl);
      return ResponseEntity.status(HttpStatus.CREATED).body(new StickerDto.Response(sticker));

    } catch (Exception e) {
      log.error("❌ 스티커 생성 실패", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * 2) AI 스티커 등록 (이미지 URL만 전달)
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<StickerDto.Response> createAISticker(@RequestBody StickerDto.CreateRequest request) {
    try {
      // AI 스티커의 경우 file 업로드 X, imageUrl 직접 전달
      if (request.getImageUrl() == null || request.getType() != StickerType.AI) {
        log.warn("⚠️ 잘못된 AI 스티커 요청: {}", request);
        return ResponseEntity.badRequest().build();
      }

      Sticker sticker = stickerService.createSticker(request.getType(), request.getImageUrl());
//      log.info("🤖 AI 스티커 등록 완료: {}", request.getImageUrl());
      return ResponseEntity.status(HttpStatus.CREATED).body(new StickerDto.Response(sticker));

    } catch (Exception e) {
      log.error("❌ AI 스티커 생성 실패", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * 스티커 전체 조회
   */
  @GetMapping
  public ResponseEntity<List<StickerDto.Response>> getStickers() {
    List<StickerDto.Response> stickers = stickerService.getStickers();
    return ResponseEntity.ok(stickers);
  }

  /**
   * 스티커 삭제
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSticker(@PathVariable Long id) {
    stickerService.deleteSticker(id);
    return ResponseEntity.noContent().build();
  }
}
