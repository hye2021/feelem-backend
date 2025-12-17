package com.feelem.server.global;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

  // 태블릿 앱에서 구독할 주제 이름 (안드로이드 코드에서 이 문자열을 똑같이 써야 함)
  private static final String KIOSK_TOPIC = "kiosk_tablet";

  public void sendPrintNotification(String s3ImageUrl) {
    try {
      // 알림 메시지 구성
      // setTopic: 특정 기기 토큰이 아니라, 이 주제를 구독 중인 모든 기기(태블릿)에 보냄
      Message message = Message.builder()
          .setTopic(KIOSK_TOPIC)
          // putData: 앱이 백그라운드에 있어도 데이터를 처리할 수 있도록 'data' 필드에 넣음
          .putData("type", "PRINT_REQUEST")
          .putData("imageUrl", s3ImageUrl)
          .build();

      // 전송
      String response = FirebaseMessaging.getInstance().send(message);
      log.info("✅ FCM 전송 성공: response={}, image={}", response, s3ImageUrl);

    } catch (Exception e) {
      log.error("❌ FCM 전송 실패: ", e);
      // 필요 시 예외를 던지거나, 실패 처리를 여기서 수행
    }
  }
}
