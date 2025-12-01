package com.feelem.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

    // 1. 연결 타임아웃 (서버가 켜져 있는지 확인하는 시간)
    // 이건 2~5초면 충분합니다. 서버가 꺼져있으면 빨리 실패하는 게 좋으니까요.
    factory.setConnectTimeout(5000); // 5초로 약간 늘림

    // 2. 읽기 타임아웃 (데이터 처리를 기다리는 시간) 👈 여기가 핵심!
    // AI가 그림 그리는 동안 기다려줄 시간입니다.
    factory.setReadTimeout(120000);  // 120000ms = 120초 = 2분

    return new RestTemplate(factory);
  }
}
