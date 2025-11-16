package com.feelem.server.domain.user.controller;

import com.feelem.server.domain.user.dto.UserMypageResponse;
import com.feelem.server.domain.user.service.UserService;
import com.feelem.server.domain.user.dto.UserDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "사용자 API", description = "사용자 정보 조회 등 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  @GetMapping("/{userId}")
  public ResponseEntity<UserDto.ProfileResponse> getUserProfile(@PathVariable Long userId) {
    UserDto.ProfileResponse response = new UserDto.ProfileResponse(userService.findById(userId));

    log.info("✔️ 사용자 프로필이 조회되었습니다: {}", response);

    return ResponseEntity.ok(response);
  }

  // 닉네임 설정 혹은 변경
  @PostMapping("/nickname")
  public ResponseEntity<Void> setOrUpdateNickname(@RequestBody Map<String, String> request) {
    String nickname = request.get("nickname");
    userService.updateNickname(nickname);

    log.info("✔️ 닉네임이 설정/변경되었습니다: {}", nickname);

    return ResponseEntity.ok().build();
  }

  // 닉네임 중복 확인
  @GetMapping("/nickname")
  public ResponseEntity<Map<String, Boolean>> checkNicknameExists(
      @RequestParam("candidate") String nickname
  ) {
    boolean exists = userService.isNicknameDuplicate(nickname);

    log.info("✔️ 닉네임 중복 여부 확인: {} -> {}", nickname, exists);

    return ResponseEntity.ok(Map.of("exists", exists));
  }


  // 가입된 회원인지 확인
  @GetMapping("/exists")
  public ResponseEntity<Map<String, Boolean>> checkUserExists() {
    boolean exists = userService.isRegisteredUser();

    log.info("✔️ 가입된 회원 여부 확인: {}", exists);

    return ResponseEntity.ok(Map.of("exists", exists));
  }

  // 마이페이지 정보 조회
  @GetMapping("/mypage")
  public ResponseEntity<UserMypageResponse> getMyPage() {

    UserMypageResponse response = userService.getMypage();

    log.info("✔️ 마이페이지 정보가 조회되었습니다: {}", response);

    return ResponseEntity.ok(response);
  }

  // 소셜 아이디 조회
  @GetMapping("/social")
  public ResponseEntity<Map<String, String>> getSocialIds() {
    Map<String, String> result = userService.getSocialIds();
    return ResponseEntity.ok(result);
  }

}