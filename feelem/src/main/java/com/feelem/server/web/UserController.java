package com.feelem.server.web;

import com.feelem.server.domain.user.UserService;
import com.feelem.server.global.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 API", description = "사용자 정보 조회 등 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  @Operation(summary = "특정 사용자 프로필 조회")
  @GetMapping("/{userId}/profile")
  public ResponseEntity<UserDto.ProfileResponse> getUserProfile(@PathVariable Long userId) {
    UserDto.ProfileResponse response = new UserDto.ProfileResponse(userService.findById(userId));
    return ResponseEntity.ok(response);
  }
}