package com.feelem.server.global.dto;

import com.feelem.server.domain.filter.Filter;
import com.feelem.server.domain.filter.PlacementType;
import lombok.*;
import java.util.List;
import java.util.Map;

public class FilterDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CreateRequest {
    private String name;
    private Integer price;
    private String originalImageUrl;
    private String editedImageUrl;
    private Integer aspectX;
    private Integer aspectY;
    private Map<String, Double> colorAdjustments;
    private List<String> tags;
    private List<StickerPlacement> stickers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StickerPlacement {
      private Long stickerId;
      private PlacementType placementType; // ABSOLUTE, FACE_TRACKING
      private Double scale;
      private Double rotation;
      private Double x;
      private Double y;
      private String anchor; // FACE_TRACKING일 때만 사용
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UpdatePriceRequest {
    private Integer price;
  }

  @Getter
  public static class Response {
    private final Long id;
    private final String name;
    private final Integer price;
    private final String originalImageUrl;
    private final String editedImageUrl;
    private final Map<String, Double> colorAdjustments;
    private final Boolean isDeleted;
    private final Long saveCount;
    private final Long useCount;

    private final List<String> tags;
    private final List<CreateRequest.StickerPlacement> stickers;

    public Response(Filter filter, List<String> tags, List<CreateRequest.StickerPlacement> stickers) {
      this.id = filter.getId();
      this.name = filter.getName();
      this.price = filter.getPrice();
      this.originalImageUrl = filter.getOriginalImageUrl();
      this.editedImageUrl = filter.getEditedImageUrl();
      this.colorAdjustments = filter.getColorAdjustments();
      this.isDeleted = filter.getIsDeleted();
      this.saveCount = filter.getSaveCount();
      this.useCount = filter.getUseCount();
      this.tags = tags;
      this.stickers = stickers;
    }
  }
}
