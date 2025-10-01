package com.feelem.server.domain.user;

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
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    // 현재 로그인 진행 중인 서비스를 구분 (google, naver, kakao...)
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    // OAuth2 로그인 진행 시 키가 되는 필드값 (PK와 같은 의미)
    String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
        .getUserInfoEndpoint().getUserNameAttributeName();

    // OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 클래스
    OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

    User user = saveOrUpdate(attributes);

    // Spring Security에서 사용할 최종 User 객체
    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
        attributes.getAttributes(),
        attributes.getNameAttributeKey());
  }

  // DB에 사용자가 있으면 업데이트, 없으면 새로 저장
  private User saveOrUpdate(OAuthAttributes attributes) {
    User user = userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
        .map(entity -> entity.update(attributes.getNickname())) // 이미 있으면 이름만 업데이트
        .orElse(attributes.toEntity()); // 없으면 새로 생성

    return userRepository.save(user);
  }
}