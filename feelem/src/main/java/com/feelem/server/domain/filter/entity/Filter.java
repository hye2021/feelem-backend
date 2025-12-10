package com.feelem.server.domain.filter.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.feelem.server.domain.sticker.entity.FaceStickerPlacement;
import com.feelem.server.domain.user.entity.Social;
import com.feelem.server.domain.user.entity.SocialType;
import com.feelem.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "filters")
public class Filter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private User creator;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer price;

  @Column(name = "original_image_url", nullable = false)
  private String originalImageUrl;

  @Column(name = "edited_image_url", nullable = false)
  private String editedImageUrl;

  @Column(name = "sticker_image_no_face_url")
  private String stickerImageNoFaceUrl;

  // 기본 지정 비율 (x:y)
  @Column(name = "aspect_x")
  private Integer aspectX;
  @Column(name = "aspect_y")
  private Integer aspectY;

  /**
   * 13가지 OpenGL 조정값 (Json)
   * brightness, exposure, contrast, highlight, shadow,
   * temperature, hue, saturation, sharpen, blur, vignette, noise
   */
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "color_adjustments", columnDefinition = "json", nullable = false)
  private Map<String, Double> colorAdjustments;

  // 삭제 여부 (soft delete)
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  // 저장(북마크), 사용(무료+유료), 구매(유료'결제'가 발생) 수
  // 초기값 0L 설정
  @Column(name = "save_count", nullable = false)
  private Long saveCount = 0L;
  @Column(name = "use_count", nullable = false)
  private Long useCount = 0L;
  @Column(name = "purchase_count", nullable = false)
  private Long purchaseCount = 0L;
  
  // 유료 필터로 총 얼마 판매되었는지 누적
  private Long totalSalesAmount = 0L;
  
  // 생성 일시
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  // sns 아이디 표기
  @Column(name = "social_type")
  private SocialType socialType;

  // Social 연결 엔티티
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "social_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Social social;

  // 태그 목록 (연결 엔티티)
  @OneToMany(mappedBy = "filter", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FilterTag> filterTags = new ArrayList<>();

  // 스티커 목록 (연결 엔티티)
  @OneToMany(mappedBy = "filter", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FaceStickerPlacement> faceStickerPlacements = new ArrayList<>();

  @Builder
  public Filter(User creator, String name, Integer price,
      String originalImageUrl, String editedImageUrl, String stickerImageNoFaceUrl,
      Integer aspectX, Integer aspectY, Map<String, Double> colorAdjustments,
      SocialType socialType, Social social) {
    this.creator = creator;
    this.name = name;
    this.price = price;
    this.originalImageUrl = originalImageUrl;
    this.editedImageUrl = editedImageUrl;
    this.stickerImageNoFaceUrl = stickerImageNoFaceUrl;
    this.aspectX = aspectX;
    this.aspectY = aspectY;
    this.colorAdjustments = colorAdjustments;
    this.socialType = socialType;
    this.social = social;
    this.createdAt = LocalDateTime.now();
    this.saveCount = 0L;
    this.useCount = 0L;
    this.purchaseCount = 0L;
    this.totalSalesAmount = 0L;
  }

  // 단순 상태 변경 메서드는 유지
  public void softDelete() {
    this.isDeleted = true;
  }

  public void updatePrice(Integer price) {
    this.price = price;
  }
}