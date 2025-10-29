package com.feelem.server.domain.sticker.entity;

import com.feelem.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stickers")
public class Sticker {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id", nullable = false)
  private User creator;

  @Enumerated(EnumType.STRING)
  @Column(name = "sticker_type", nullable = false)
  private StickerType stickerType;

  @Column(name = "sticker_image_url", nullable = false)
  private String imageUrl;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder
  public Sticker(User creator, StickerType stickerType, String imageUrl) {
    this.creator = creator;
    this.stickerType = stickerType;
    this.imageUrl = imageUrl;
    this.isDeleted = false;
    this.createdAt = LocalDateTime.now();
  }

  public void markAsDeleted() {
    this.isDeleted = true;
  }
}
