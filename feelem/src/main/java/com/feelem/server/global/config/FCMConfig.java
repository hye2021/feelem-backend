package com.feelem.server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class FCMConfig {

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    // 이미 초기화되어 있다면(애플리케이션 재시작 등 상황) 기존 인스턴스 반환
    List<FirebaseApp> apps = FirebaseApp.getApps();
    if (apps != null && !apps.isEmpty()) {
      return apps.get(0);
    }

    ClassPathResource resource = new ClassPathResource("feelem-print-firebase-adminsdk-fbsvc-ef7a2e241b.json");
    InputStream refreshToken = resource.getInputStream();

    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(refreshToken))
        .build();

    return FirebaseApp.initializeApp(options);
  }
}