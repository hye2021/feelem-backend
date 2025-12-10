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

    // 연결 타임아웃 (서버가 켜져 있는지 확인하는 시간)
    factory.setConnectTimeout(5000);

    // 읽기 타임아웃 (데이터 처리를 기다리는 시간)
    factory.setReadTimeout(120000);  // 120000ms = 120초 = 2분

    return new RestTemplate(factory);
  }
}
