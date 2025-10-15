package com.feelem.server.global.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class S3Uploader {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public String upload(MultipartFile file, String key) throws IOException {
    s3Client.putObject(PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(file.getContentType())
        .acl(ObjectCannedACL.PUBLIC_READ)
        .build(), software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

    return String.format("https://%s.s3.amazonaws.com/%s", bucket, key);
  }

  public void delete(String fileUrl) {
    String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);
    s3Client.deleteObject(DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build());
  }
}
