package com.feelem.server.domain.filter.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterCreateRequest {

  private String name;
  private Integer price;
  private List<String> tags;
  private String socialType;

  private String originalImageUrl;
  private String editedImageUrl;
  private String stickerImageNoFaceUrl;

  private Integer aspectX;
  private Integer aspectY;
  private Map<String, Double> colorAdjustments;

  private List<FilterCreateRequest.FaceSticker> stickers;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public class FaceSticker {

    private Long stickerId;
    private double relX;
    private double relY;
    private double relW;
    private double relH;
    private double rot;
  }
}
