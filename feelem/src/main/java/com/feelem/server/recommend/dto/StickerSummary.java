package com.feelem.server.recommend.dto;

import java.util.List;

public record StickerSummary(
    int count,                      // 스티커 개수
    List<String> placement_types,   // 배치 구역 (예: "TOP_LEFT")
    boolean has_face_sticker        // 얼굴 인식 스티커 사용 여부
) {}
