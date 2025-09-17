package com.feelem.server.domain.sticker;

import com.feelem.server.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

  @Column(name = "sticker_type", nullable = false)
  private String stickerType;

  @Column(name = "sticker_image_url", nullable = false)
  private String stickerImageUrl;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  // ai_prompt 등 ERD에 있는 다른 필드들도 필요에 따라 추가
}
