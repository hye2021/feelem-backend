package com.feelem.server.domain.user.service;

import com.feelem.server.domain.user.entity.Point;
import com.feelem.server.domain.user.entity.Social;
import com.feelem.server.domain.user.repository.PointRepository;
import com.feelem.server.domain.user.repository.SocialRepository;
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
  private final PointRepository pointRepository;
  private final SocialRepository socialRepository; // ⭐ 추가

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

  private User saveOrUpdate(OAuthAttributes attributes) {
    // 1. DB에서 유저를 찾습니다.
    User user = userRepository.findByProviderAndProviderId(
            attributes.getProvider(),
            attributes.getProviderId())
        .map(entity -> entity.update(attributes.getNickname())) // A. 기존 유저라면 닉네임만 수정
        .orElseGet(() -> attributes.toEntity()); // B. 신규 유저라면 객체 생성 (이때 생성자에서 Social, Point도 자동 생성됨)

    // 2. 유저를 저장합니다.
    // (User 엔티티의 Cascade 설정 덕분에 연결된 Social, Point도 DB에 같이 저장됩니다!)
    return userRepository.save(user);
  }
}
