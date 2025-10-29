package com.feelem.server.domain.upload.service;

import com.feelem.server.domain.upload.repository.UploadRepository;
import com.feelem.server.domain.upload.entity.UploadedFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {

  private final UploadRepository uploadRepository;
  private final UploadService uploadService;

  // 매일 새벽 3시, 6시간 이상 사용되지 않은 업로드 파일 정리
  @Scheduled(cron = "0 0 3 * * *")
  public void cleanupUnusedFiles() {
    LocalDateTime cutoff = LocalDateTime.now().minusHours(6);
    List<UploadedFile> unused = uploadRepository
        .findAllByUsedFalseAndUploadedAtBefore(cutoff);

    if (unused.isEmpty()) return;

    log.info("🧹 Cleaning up {} unused uploaded files...", unused.size());

    for (UploadedFile file : unused) {
      try {
        uploadService.delete(file.getFileUrl());
        log.info("Deleted unused file: {}", file.getFileUrl());
      } catch (Exception e) {
        log.warn("Failed to delete file: {}", file.getFileUrl(), e);
      }
    }
  }
}
