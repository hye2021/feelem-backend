package com.feelem.server.domain.upload;

import com.feelem.server.global.common.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

  private final UploadRepository uploadRepository;
  private final S3Uploader s3Uploader;

  // 스티커 이미지를 S3에 업로드하고, 업로드 정보를 DB에 저장
  public String uploadSticker(MultipartFile file) throws IOException {
    // 파일명 고유화
    String key = "stickers/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

    // S3에 업로드
    String url = s3Uploader.upload(file, key);

    // DB에 기록 (used = false)
    uploadRepository.save(new UploadedFile(url));

    return url;
  }

  public void deleteFile(String url) {
    s3Uploader.delete(url);
    uploadRepository.findByFileUrl(url)
        .ifPresent(uploadRepository::delete);
  }

  public void markUsed(String fileUrl) {
    uploadRepository.findByFileUrl(fileUrl)
        .ifPresent(UploadedFile::markUsed);
  }
}
