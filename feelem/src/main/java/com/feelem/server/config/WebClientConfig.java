package com.feelem.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Value("${recommend.server.url}")
  private String recommendServerUrl;

  @Bean
  public WebClient recommendationWebClient() {
    return WebClient.builder()
        .baseUrl(recommendServerUrl) // application.yml에 설정한 URL
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}