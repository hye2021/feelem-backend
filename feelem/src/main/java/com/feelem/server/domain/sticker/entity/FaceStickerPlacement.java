package com.feelem.server.domain.sticker.entity;

import com.feelem.server.domain.filter.entity.Filter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "face_sticker_placements")
public class FaceStickerPlacement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "filter_id", nullable = false)
  private Filter filter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sticker_id", nullable = false)
  private Sticker sticker;

  @Column
  private Double relX;

  @Column
  private Double relY;

  @Column
  private Double relW;

  @Column
  private Double relH;

  @Column
  private Double rot;

  public FaceStickerPlacement(Filter filter, Sticker sticker, Double relX, Double relY, Double relW, Double relH, Double rot) {
    this.filter = filter;
    this.sticker = sticker;
    this.relX = relX;
    this.relY = relY;
    this.relW = relW;
    this.relH = relH;
    this.rot = rot;
  }

}
