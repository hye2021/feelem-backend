package com.feelem.server.domain.user.service;

import com.feelem.server.domain.user.entity.Point;
import com.feelem.server.domain.user.repository.PointRepository;
import com.feelem.server.domain.user.OAuthAttributes;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;
  private final PointRepository pointRepository; // ⭐ 추가

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    String userNameAttributeName = userRequest.getClientRegistration()
        .getProviderDetails()
        .getUserInfoEndpoint()
        .getUserNameAttributeName();

    OAuthAttributes attributes =
        OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

    User user = saveOrUpdate(attributes);

    // 반환 attributes
    Map<String, Object> customAttributes = new HashMap<>(attributes.getAttributes());
    customAttributes.put("provider", attributes.getProvider());
    customAttributes.put("providerId", attributes.getProviderId());
    customAttributes.put("user_id", user.getId());

    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
        customAttributes,
        attributes.getNameAttributeKey()
    );
  }

  /**
   * DB에 사용자가 있으면 이름만 업데이트하고,
   * 없으면 신규 User를 만들고 포인트도 생성한다.
   */
  private User saveOrUpdate(OAuthAttributes attributes) {

    return userRepository.findByProviderAndProviderId(
            attributes.getProvider(),
            attributes.getProviderId()
        )
        .map(entity -> {
          // 기존 사용자 → 업데이트
          entity.update(attributes.getNickname());
          return entity;
        })
        .orElseGet(() -> {
          // 신규 생성
          User newUser = attributes.toEntity();
          User savedUser = userRepository.save(newUser);

          // ⭐ 신규User 생성 시 포인트 동시에 생성
          pointRepository.save(new Point(savedUser, 0));

          return savedUser;
        });
  }
}
