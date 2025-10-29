package com.feelem.server.domain.user.dto;

import com.feelem.server.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class UserDto {

  @Getter
  @Schema(description = "사용자 프로필 응답")
  public static class ProfileResponse {

    @Schema(description = "사용자 ID", example = "1")
    private final Long id;

    @Schema(description = "닉네임", example = "개발자 필름")
    private final String nickname;

    public ProfileResponse(User user) {
      this.id = user.getId();
      this.nickname = user.getNickname();
    }
  }
}