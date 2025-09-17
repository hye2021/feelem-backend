package com.feelem.server.global.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FilterDto {
  @Getter
  @NoArgsConstructor
  @Schema(description = "필터 생성을 위한 요청 데이터 dto")
  public static class CreateRequest {
    @Schema(description = "필터 이름", example = "My Cool Filter")
    private String name;

    @Schema(description = "필터 가격", example = "100")
    private Integer price;

    @Schema(description = "필터 공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(description = "13가지 색감 조절 값")
    private Map<String, Double> colorAdjustments;

    @Schema(description = "필터에 달린 태그 목록 (최대5개)", example = "[\"vintage\", \"bright\"]")
    private List<String> tags;

    @Schema(description = "사용된 스티커와 배치 정보 목록")
    private List<StickerPlacementRequest> stickers;
  }

  @Getter
  @NoArgsConstructor
  @Schema(description = "스티커 배치 정보를 담는 객체")
  public static class StickerPlacementRequest {
    @Schema(description = "재사용할 스티커의 ID", example = "1")
    private Long stickerId;

    @Schema(description = "스티커 배치 정보 JSON", example = "{\"position\": {\"x\": 0.5, \"y\": 0.5}, \"scale\": 1.0, \"rotation\": 0.0}")
    private JsonNode placementInfo;
  }
}
