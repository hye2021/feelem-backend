package com.feelem.server.domain.upload.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String fileUrl;

  @Column(nullable = false)
  private boolean used = false;

  @Column(nullable = false, updatable = false)
  private LocalDateTime uploadedAt = LocalDateTime.now();

  public UploadedFile(String fileUrl) {
    this.fileUrl = fileUrl;
  }

  public void markUsed() {
    this.used = true;
  }
}
