package com.feelem.server.domain.sticker.service;

import com.feelem.server.domain.upload.repository.UploadRepository;
import com.feelem.server.domain.upload.entity.UploadedFile;
import com.feelem.server.global.common.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * AI 스티커 생성 및 다시 만들기 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class StickerAIService {

  private final S3Uploader s3Uploader;
  private final UploadRepository uploadRepository;

  /**
   * ✅ 더미 이미지를 S3에 업로드하고 UploadedFile로 저장
   */
  public Map<String, Object> generateDummySticker(String prompt) throws IOException {
    // 1️⃣ 더미 이미지 파일 읽기
    File dummy = new File("src/main/resources/static/dummy.jpg");
    if (!dummy.exists()) {
      throw new FileNotFoundException("dummy.png not found in resources/static/");
    }

    // 2️⃣ 파일명 생성
    String fileName = "ai-sticker-" + UUID.randomUUID() + ".png";

    // 3️⃣ S3 업로드
    String fileUrl = s3Uploader.upload(dummy, "stickers/ai");

    // 4️⃣ DB 저장
    UploadedFile uploadedFile = new UploadedFile(fileUrl);
    uploadRepository.save(uploadedFile);

    // 5️⃣ 응답 데이터 구성 (id + url)
    Map<String, Object> response = new HashMap<>();
    response.put("id", uploadedFile.getId());
    response.put("imageUrl", uploadedFile.getFileUrl());

    return response;
  }

  /**
   * ✅ 다시 만들기: 기존 업로드된 파일만 삭제
   */
  public void deleteGeneratedSticker(Long id) {
    Optional<UploadedFile> uploadedOpt = uploadRepository.findById(id);
    if (uploadedOpt.isPresent()) {
      UploadedFile file = uploadedOpt.get();

      // S3에서 삭제
      s3Uploader.delete(file.getFileUrl());

      // DB에서도 삭제
      uploadRepository.delete(file);
    }
  }
}
