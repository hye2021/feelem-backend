package com.feelem.server.domain.upload;

import com.feelem.server.global.common.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadService {

  private final S3Uploader s3Uploader;

  public String uploadSticker(MultipartFile file) throws Exception {
    return s3Uploader.upload(file, "stickers");
  }

  public void delete(String fileUrl) {
    s3Uploader.delete(fileUrl);
  }
}
