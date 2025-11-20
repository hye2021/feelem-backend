package com.feelem.server.domain.user.entity;

public enum SocialType {
  NONE,
  INSTAGRAM,
  X;

  public static SocialType fromString(String value) {
    for (SocialType type : SocialType.values()) {
      if (type.name().equalsIgnoreCase(value)) {
        return type;
      }
    }
    return NONE;
  }
}
