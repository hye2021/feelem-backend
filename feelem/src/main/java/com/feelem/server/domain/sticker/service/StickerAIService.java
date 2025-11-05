package com.feelem.server.domain.sticker.service;

import com.feelem.server.domain.upload.entity.UploadedFile;
import com.feelem.server.domain.upload.repository.UploadRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StickerAIService {

  private final UploadRepository uploadRepository;
  private final RestTemplate restTemplate;

  @Value("${generate.server.url}")
  private String aiServerUrl;

  public Map<String, Object> generateSticker(String prompt) throws IOException {
    try {
      log.info("🎨 AI 서버에 스티커 생성 요청: {}", prompt);

      String url = aiServerUrl + "/gensticker";
      Map<String, String> body = Map.of("prompt_ko", prompt);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, Object> resBody = response.getBody();
        String s3Url = (String) resBody.get("url");

        if (s3Url == null || s3Url.isEmpty()) {
          throw new IOException("AI 서버 응답에 이미지 url 누락");
        }

        UploadedFile uploadedFile = new UploadedFile(s3Url);
        uploadRepository.save(uploadedFile);

        Map<String, Object> result = new HashMap<>();
        result.put("id", uploadedFile.getId());
        result.put("imageUrl", uploadedFile.getFileUrl());
        result.put("fromAIServer", true);

        log.info("✅ AI 서버 스티커 생성 성공: {}", s3Url);
        return result;
      }

      log.warn("⚠️ AI 서버 응답 오류: {}", response.getStatusCode());
      return generateDummySticker(prompt);

    } catch (Exception e) {
      log.error("❌ AI 서버 요청 실패: {}", e.getMessage());
      return generateDummySticker(prompt);
    }
  }

  private Map<String, Object> generateDummySticker(String prompt) {
    String dummyUrl = "https://feelem-s3-bucket.s3.ap-northeast-2.amazonaws.com/static/dummy.jpg";

    // ✅ 이미 존재하는 더미 파일이 있다면 재사용
    Optional<UploadedFile> existing = uploadRepository.findByFileUrl(dummyUrl);
    UploadedFile uploadedFile = existing.orElseGet(() -> uploadRepository.save(new UploadedFile(dummyUrl)));

    Map<String, Object> response = new HashMap<>();
    response.put("id", uploadedFile.getId());
    response.put("imageUrl", uploadedFile.getFileUrl());
    response.put("dummy", true);

    log.info("🪄 더미 스티커 생성 완료 (프롬프트: {})", prompt);
    return response;
  }


  public void deleteGeneratedSticker(Long id) {
    uploadRepository.findById(id).ifPresent(file -> {
      uploadRepository.delete(file);
      log.info("🗑️ AI 스티커 삭제 완료 (ID: {})", id);
    });
  }
}
