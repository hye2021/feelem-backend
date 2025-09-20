package com.feelem.server.domain.filter;

import com.feelem.server.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.type.SqlTypes;

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
  private User creator;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false)
  private Integer price;

  @Column(name = "preview_url")
  private String previewUrl;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "color_adjustments", columnDefinition = "json", nullable = false)
  private Map<String, Double> colorAdjustments;

  @Column(name = "is_public", nullable = false)
  private Boolean isPublic;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @OneToMany(mappedBy = "filter", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FilterTag> filterTags = new ArrayList<>();

  @OneToMany(mappedBy = "filter", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<FilterSticker> filterStickers = new ArrayList<>();

  @Builder
  public Filter(User creator, String name, String description, Integer price, Boolean isPublic, Map<String, Double> colorAdjustments, String previewUrl) {
    this.creator = creator;
    this.name = name;
    this.description = description;
    this.price = price;
    this.isPublic = isPublic;
    this.colorAdjustments = colorAdjustments;
    this.previewUrl = previewUrl;
  }
}