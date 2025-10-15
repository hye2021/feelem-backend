package com.feelem.server.global.common;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public String upload(MultipartFile multipartFile, String dirName) throws IOException {
    File uploadFile = convert(multipartFile)
        .orElseThrow(() -> new IllegalArgumentException("파일 변환 실패"));

    String key = dirName + "/" + UUID.randomUUID() + "-" + uploadFile.getName();
    amazonS3.putObject(new PutObjectRequest(bucket, key, uploadFile));
    uploadFile.delete();

    return amazonS3.getUrl(bucket, key).toString();
  }

  public void delete(String fileUrl) {
    String key = extractKeyFromUrl(fileUrl);
    amazonS3.deleteObject(bucket, key);
  }

  private Optional<File> convert(MultipartFile file) throws IOException {
    File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
    try (FileOutputStream fos = new FileOutputStream(convFile)) {
      fos.write(file.getBytes());
    }
    return Optional.of(convFile);
  }

  private String extractKeyFromUrl(String fileUrl) {
    int index = fileUrl.indexOf(".com/");
    return fileUrl.substring(index + 5);
  }
}
