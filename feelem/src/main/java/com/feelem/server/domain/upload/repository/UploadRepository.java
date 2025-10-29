package com.feelem.server.domain.upload.repository;

import com.feelem.server.domain.upload.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UploadRepository extends JpaRepository<UploadedFile, Long> {
  Optional<UploadedFile> findByFileUrl(String fileUrl);

  List<UploadedFile> findAllByUsedFalseAndUploadedAtBefore(LocalDateTime cutoff);
}
