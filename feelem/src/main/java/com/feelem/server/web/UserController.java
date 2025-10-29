package com.feelem.server.web;

import com.feelem.server.domain.sticker.Sticker;
import com.feelem.server.domain.user.UserService;
import com.feelem.server.global.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 API", description = "사용자 정보 조회 등 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  @GetMapping("/{userId}")
  public ResponseEntity<UserDto.ProfileResponse> getUserProfile(@PathVariable Long userId) {
    UserDto.ProfileResponse response = new UserDto.ProfileResponse(userService.findById(userId));
    return ResponseEntity.ok(response);
  }

  // 닉네임 설정 혹은 변경
  @PostMapping("/nickname")
  public ResponseEntity<Void> setOrUpdateNickname(@RequestBody Map<String, String> request) {
    String nickname = request.get("nickname");
    userService.updateNickname(nickname);
    return ResponseEntity.ok().build();
  }

  // 가입된 회원인지 확인
  @GetMapping("/exists")
  public ResponseEntity<Map<String, Boolean>> checkUserExists() {
    boolean exists = userService.isRegisteredUser();
    return ResponseEntity.ok(Map.of("exists", exists));
  }

}