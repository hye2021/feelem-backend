package com.feelem.server.domain.filter.dto;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.user.entity.SocialType;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class FilterResponse {

  private Boolean isMine;
  private Boolean isUsed;

  private Long id;
  private String name;
  private String creator;
  private Integer price;
  private List<String> tags;

  private String originalImageUrl;
  private String editedImageUrl;
  private String stickerImageNoFaceUrl;

  private Integer aspectX;
  private Integer aspectY;
  private Map<String, Double> colorAdjustments;
  private List<FaceStickerResponse> stickers;

  private Long saveCount;
  private Long useCount;

  private String socialType;   // 대표 소셜 종류
  private String socialValue;  // 대표 소셜 ID 값

  public FilterResponse(Filter filter, Boolean isMine, Boolean isUsed, List<String> tags,
      List<FaceStickerResponse> stickers) {
    this.isMine = isMine;
    this.isUsed = isUsed;
    this.id = filter.getId();
    this.name = filter.getName();
    this.creator = filter.getCreator().getNickname();
    this.price = filter.getPrice();
    this.tags = tags;
    this.originalImageUrl = filter.getOriginalImageUrl();
    this.editedImageUrl = filter.getEditedImageUrl();
    this.stickerImageNoFaceUrl = filter.getStickerImageNoFaceUrl();
    this.aspectX = filter.getAspectX();
    this.aspectY = filter.getAspectY();
    this.colorAdjustments = filter.getColorAdjustments();
    this.stickers = stickers;
    this.saveCount = filter.getSaveCount();
    this.useCount = filter.getUseCount();

    // sns 아이디
    SocialType type = filter.getSocialType();
    this.socialType = type.toString();
    if (type == SocialType.NONE) {
      this.socialValue = "";
    } else if (type == SocialType.INSTAGRAM) {
      this.socialValue = filter.getSocial().getInstagramId();
    } else if (type == SocialType.X) {
      this.socialValue = filter.getSocial().getXId();
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class FaceStickerResponse {

    private Long stickerId;
    private String stickerImageUrl;
    private double relX;
    private double relY;
    private double relW;
    private double relH;
    private double rot;
  }
}
