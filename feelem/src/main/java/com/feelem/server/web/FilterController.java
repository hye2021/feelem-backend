package com.feelem.server.web;

import com.feelem.server.domain.filter.FilterService;
import com.feelem.server.global.dto.FilterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "필터 API", description = "필터 생성, 조회 등 필터 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/filters")
public class FilterController {

  private final FilterService filterService;

  @Operation(summary = "신규 필터 생성", description = "사용자가 제작한 필터 정보를 시스템에 저장합니다.")
  @ApiResponse(responseCode = "201", description = "필터 생성 성공")
  @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content)
  @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
  @PostMapping
  public ResponseEntity<Void> createFilter(@Valid @RequestBody FilterDto.CreateRequest request) {
    // TODO: Spring Security 연동 후 실제 로그인한 사용자 ID를 가져와야 합니다.
    // 예시: Long currentUserId = ((PrincipalDetails) authentication.getPrincipal()).getUser().getId();
    Long currentUserId = 1L; // 지금은 테스트를 위해 임시 사용자 ID (1번)를 사용합니다.

    Long newFilterId = filterService.createFilter(request, currentUserId);

    // 생성된 리소스의 위치(URI)를 헤더에 담아 반환합니다.
    URI location = URI.create(String.format("/api/v1/filters/%d", newFilterId));
    return ResponseEntity.created(location).build();
  }

  // 여기에 필터 조회, 수정, 삭제 등의 API 메서드를 추가해 나갈 수 있습니다.
}