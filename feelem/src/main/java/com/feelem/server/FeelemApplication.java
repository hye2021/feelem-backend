package com.feelem.server;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FeelemApplication {

  // 앱 실행 시 타임존을 '서울'로 고정
  @PostConstruct
  public void started() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
  }

  public static void main(String[] args) {
    SpringApplication.run(FeelemApplication.class, args);
  }

}
