package com.feelem.server.domain.user.service;

import com.feelem.server.config.jwt.JwtTokenProvider;
import com.feelem.server.config.jwt.TokenInfo;
import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.user.dto.UserMypageResponse;
import com.feelem.server.domain.user.entity.Point;
import com.feelem.server.domain.user.entity.Social;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.PointRepository;
import com.feelem.server.domain.user.repository.SocialRepository;
import com.feelem.server.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PointRepository pointRepository;
  private final SocialRepository socialRepository;

  @Transactional(readOnly = true)
  public User findById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));
  }

  public User getCurrentUser() {
//    log.info(" 현재 요청 보낸 사용자 조회 (JWT기반)");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // ✅ OAuth 로그인한 유저는 SecurityContext에서 email 기반으로 가져오기
    if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
      throw new RuntimeException("로그인이 필요합니다.");
    }

    String name = authentication.getName();
    log.info("👩‍🦰 현재 요청 하는 user의 id: {}", name);

    // ✅ 숫자인 경우 ID로 조회
    try {
      Long userId = Long.parseLong(name);
      return userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
    } catch (NumberFormatException e) {
      // ✅ 이메일일 경우 기존 방식 유지
      return userRepository.findByEmail(name)
          .orElseThrow(() -> new RuntimeException("User not found"));
    }
  }

  @Transactional
  public TokenInfo reissueToken(String refreshToken) {
    // 1. Refresh Token 유효성 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new IllegalArgumentException("유효하지 않은 Refresh Token 입니다.");
    }

    // 2. DB에 저장된 Refresh Token과 일치하는 사용자를 찾는다.
    User user = userRepository.findByRefreshToken(refreshToken)
        .orElseThrow(() -> new IllegalArgumentException("Refresh Token에 해당하는 사용자가 없습니다."));

    // 3. ⬇️ DB에서 찾은 사용자 정보로 새로운 Authentication 객체를 생성한다.
    Collection<? extends GrantedAuthority> authorities =
        Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey()));

    // 사용자 ID를 principal로 사용하여 Authentication 객체 생성
    Authentication authentication = new UsernamePasswordAuthenticationToken(user.getId().toString(), null, authorities);

    // 4. 새로운 Access Token과 Refresh Token을 생성한다 (Refresh Token Rotation).
    TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication, user.getId());

    // 5. DB에 새로운 Refresh Token으로 업데이트한다.
    user.updateRefreshToken(newTokenInfo.getRefreshToken());

    return newTokenInfo;
  }


  // 닉네임 설정 혹은 변경
  @Transactional
  public User updateNickname(String newNickname) {
    User currentUser = getCurrentUser();
    Long userId = currentUser.getId();
    User user = findById(userId);
    user.update(newNickname);

    // 이 시점에서 social과 point가 없으면 추가하는 로직 명시적으로 추가!!
    socialRepository.findByUser(user)
        .orElseGet(() -> socialRepository.save(new Social(user, null, null)));
    pointRepository.findByUserId(userId)
        .orElseGet(() -> pointRepository.save(new Point(user, 0)));

    return user;
  }

  // 닉네임 중복 확인
  @Transactional(readOnly = true)
  public boolean isNicknameDuplicate(String nickname) {
    return userRepository.existsByNickname(nickname);
  }

  // 가입된 회원인지 확인: 닉네임이 User_로 시작하는지 확인
  @Transactional(readOnly = true)
  public boolean isRegisteredUser() {
    User currentUser = getCurrentUser();
    Long userId = currentUser.getId();
    User user = findById(userId);
    return !user.getNickname().startsWith("User_");
  }

  // 보유 포인트 반환
  @Transactional(readOnly = true)
  public int getUserPoints() {
    User currentUser = getCurrentUser();
    Long userId = currentUser.getId();
    Point point = pointRepository.findByUserId(userId)
        .orElseThrow(() -> new EntityNotFoundException("포인트 정보를 찾을 수 없습니다: " + userId));
    return point.getAmount();
  }

  // 마이페이지 dto
  @Transactional(readOnly = true)
  public UserMypageResponse getMypage() {

    User user = getCurrentUser();

    Point point = pointRepository.findByUserId(user.getId())
        .orElseThrow(() -> new IllegalStateException(
            "포인트 정보를 찾을 수 없습니다. userId=" + user.getId()
        ));

    return UserMypageResponse.builder()
        .userId(user.getId())
        .nickname(user.getNickname())
        .pointAmount(point.getAmount())
        .build();
  }

  // 소셜 아이디 조회
  @Transactional(readOnly = true)
  public Map<String, String> getSocialIds() {

    User user = getCurrentUser();

    Social social = socialRepository.findByUser(user)
        .orElse(null);

    String instagramId = (social != null) ? social.getInstagramId() : null;
    String xId = (social != null) ? social.getXId() : null;

    Map<String, String> result = new HashMap<>();
    result.put("instagramId", instagramId);
    result.put("xId", xId);

    return result;
  }


  // 소셜 아이디 업데이트
  @Transactional
  public void updateSocialIds(String instagramId, String xId) {

    User user = getCurrentUser();

    Social social = socialRepository.findByUser(user)
        .orElseGet(() -> new Social(user, null, null));

    social.update(instagramId, xId);

    socialRepository.save(social);
  }

  @Transactional(readOnly = true)
  public Social getSocialByUser(User user) {
    return socialRepository.findByUser(user)
        .orElseThrow(() -> new EntityNotFoundException("소셜 정보를 찾을 수 없습니다: " + user.getId()));
  }
}