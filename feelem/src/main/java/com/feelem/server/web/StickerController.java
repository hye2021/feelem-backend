package com.feelem.server.web;

import com.feelem.server.domain.sticker.Sticker;
import com.feelem.server.domain.sticker.StickerService;
import com.feelem.server.global.dto.StickerDto;
import io.swagger.v3.oas.annotations.Operation;
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
  public ResponseEntity<Sticker> createSticker(@RequestBody StickerDto.CreateRequest request) {
    Sticker sticker = stickerService.createSticker(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(sticker);
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
