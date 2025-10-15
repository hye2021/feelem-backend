package com.feelem.server.domain.upload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Tag(name = "Upload", description = "파일 업로드 API")
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

  private final UploadService uploadService;

  @Operation(summary = "스티커 이미지 업로드", description = "S3에 스티커 이미지를 업로드하고 URL을 반환합니다.")
  @ApiResponse(responseCode = "200", description = "업로드 성공",
      content = @Content(schema = @Schema(example = "{ \"imageUrl\": \"https://cdn.feel-em.com/stickers/uuid.png\" }")))
  @PostMapping("/stickers")
  public ResponseEntity<Map<String, String>> uploadSticker(@RequestParam("file") MultipartFile file)
      throws IOException {

    String url = uploadService.uploadSticker(file);
    return ResponseEntity.ok(Map.of("imageUrl", url));
  }
}
