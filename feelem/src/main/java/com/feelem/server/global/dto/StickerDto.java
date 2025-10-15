package com.feelem.server.global.dto;

import com.feelem.server.domain.sticker.Sticker;
import com.feelem.server.domain.sticker.StickerType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class StickerDto {

  @Getter
  @Setter
  @NoArgsConstructor
  public static class CreateRequest {
    private StickerType type;
    private String imageUrl;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class Response {
    private Long id;
    private String imageUrl;

    public Response(Sticker sticker) {
      this.id = sticker.getId();
      this.imageUrl = sticker.getImageUrl();
    }
  }
}
