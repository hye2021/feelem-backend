package com.feelem.server.domain.sticker.dto;

import com.feelem.server.domain.sticker.entity.Sticker;
import com.feelem.server.domain.sticker.entity.StickerType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class StickerDto {
  @Getter
  @Setter
  @NoArgsConstructor
  public static class CreateRequest {
    private String imageUrl;
    private StickerType type;

    public String getImageUrl() {
      return imageUrl;
    }

    public StickerType getType() {
      return type;
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Response {
    private Long id;
    private String imageUrl;
    private StickerType type;

    public Response(Sticker sticker) {
      this.id = sticker.getId();
      this.imageUrl = sticker.getImageUrl();
      this.type = sticker.getStickerType();
    }
  }
}
