package com.feelem.server.domain.sticker.controller;

import com.feelem.server.domain.sticker.entity.Sticker;
import com.feelem.server.domain.sticker.service.StickerService;
import com.feelem.server.domain.sticker.dto.StickerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stickers")
public class StickerController {

  private final StickerService stickerService;

  @PostMapping
  public ResponseEntity<StickerDto.Response> createSticker(@RequestBody StickerDto.CreateRequest request) {
    System.out.print("✅ Creating sticker with imageUrl: " + request.getImageUrl() + "\n");
    Sticker sticker = stickerService.createSticker(request);
    StickerDto.Response response = new StickerDto.Response(sticker);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<StickerDto.Response>> getStickers() {
    List<StickerDto.Response> stickers = stickerService.getStickers();
    return ResponseEntity.ok(stickers);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSticker(@PathVariable Long id) {
    stickerService.deleteSticker(id);
    return ResponseEntity.noContent().build();
  }
}
