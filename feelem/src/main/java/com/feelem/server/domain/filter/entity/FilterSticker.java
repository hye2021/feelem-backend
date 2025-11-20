//package com.feelem.server.domain.filter.entity;
//
//import com.feelem.server.domain.sticker.entity.Sticker;
//import jakarta.persistence.*;
//import lombok.AccessLevel;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Entity
//@Table(name = "filter_stickers")
//public class FilterSticker {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  private Long id;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "filter_id", nullable = false)
//  private Filter filter;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = "sticker_id", nullable = false)
//  private Sticker sticker;
//
//  // 절대: 절대 스케일
//  // 얼굴 추적: 얼굴 크기에 대한 비율
//  @Column(name = "scale", nullable = false)
//  private Double scale = 1d;
//
//  @Enumerated(EnumType.STRING)
//  @Column(name = "placement_type", nullable = false)
//  private PlacementType placementType;
//
//  // 절대: 절대 좌표
//  // 얼굴 추적: 기준점으로부터의 오프셋
//  @Column(name = "x", nullable = true)
//  private Double x;
//  @Column(name = "y", nullable = true)
//  private Double y;
//  @Column(name = "rotation")
//  private Double rotation = 0d;
//
//  // 얼굴 추적: 기준점 (이마, 왼쪽 눈, 오른쪽 눈)
//  @Column(name = "anchor", nullable = true)
//  private String anchor;
//
//  @Builder
//  public FilterSticker(Filter filter, Sticker sticker, PlacementType placementType, Double scale, Double rotation, Double x, Double y, String anchor) {
//    this.filter = filter;
//    this.sticker = sticker;
//    this.placementType = placementType;
//    this.scale = scale;
//    this.rotation = rotation;
//    this.x = x;
//    this.y = y;
//    this.anchor = anchor;
//  }
//}