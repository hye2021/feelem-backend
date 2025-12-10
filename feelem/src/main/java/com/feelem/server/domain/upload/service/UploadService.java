package com.feelem.server.domain.upload.service;

import com.feelem.server.domain.upload.repository.UploadRepository;
import com.feelem.server.domain.upload.entity.UploadedFile;
import com.feelem.server.global.common.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadService {

  private final S3Uploader s3Uploader;
  private final UploadRepository uploadRepository;

  @Transactional
  public String uploadSticker(MultipartFile file) throws Exception {
    // 1) S3 업로드
    String fileUrl = s3Uploader.upload(file, "stickers");

    // 2) DB 저장
    uploadRepository.save(new UploadedFile(fileUrl));

    // 3) URL 반환
    return fileUrl;
  }

  @Transactional
  public String uploadStickerImage(MultipartFile file) throws Exception {
    // 1) S3 업로드
    String fileUrl = s3Uploader.upload(file, "stickerImagesForFilter");

    // 2) DB 저장
    uploadRepository.save(new UploadedFile(fileUrl));

    // 3) URL 반환
    return fileUrl;
  }

  @Transactional
  public String uploadFilterPreview(MultipartFile file) throws Exception {
    // 1) S3 업로드
    String fileUrl = s3Uploader.upload(file, "filter-previews");

    // 2) DB 저장
    uploadRepository.save(new UploadedFile(fileUrl));

    // 3) URL 반환
    return fileUrl;
  }

  @Transactional
  public String uploadFilterOriginal(MultipartFile file) throws Exception {
    // 1) S3 업로드
    String fileUrl = s3Uploader.upload(file, "filter-originals");

    // 2) DB 저장
    uploadRepository.save(new UploadedFile(fileUrl));

    // 3) URL 반환
    return fileUrl;
  }

  @Transactional
  public String uploadFilterReview(MultipartFile file) throws Exception {
    // 1) S3 업로드
    String fileUrl = s3Uploader.upload(file, "filter-reviews");

    // 2) DB 저장
    uploadRepository.save(new UploadedFile(fileUrl));

    // 3) URL 반환
    return fileUrl;
  }

  @Transactional
  public void delete(String fileUrl) {
    s3Uploader.delete(fileUrl);
    // DB에서도 삭제하고 싶다면:
    // uploadRepository.findByFileUrl(fileUrl).ifPresent(uploadRepository::delete);
  }
}
