package com.feelem.server.domain.filter;

import com.feelem.server.domain.sticker.Sticker;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "filter_stickers")
public class FilterSticker {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "filter_id", nullable = false)
  private Filter filter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sticker_id", nullable = false)
  private Sticker sticker;

  @Type(JsonType.class)
  @Column(name = "placement_info", columnDefinition = "json", nullable = false)
  private JsonNode placementInfo;

  @Builder
  public FilterSticker(Filter filter, Sticker sticker, JsonNode placementInfo) {
    this.filter = filter;
    this.sticker = sticker;
    this.placementInfo = placementInfo;
  }
}