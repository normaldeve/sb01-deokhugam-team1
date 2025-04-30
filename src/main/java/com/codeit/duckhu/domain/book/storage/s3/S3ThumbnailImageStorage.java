package com.codeit.duckhu.domain.book.storage.s3;

import com.codeit.duckhu.domain.book.exception.BookException;
import com.codeit.duckhu.domain.book.storage.ThumbnailImageStorage;
import com.codeit.duckhu.global.exception.ErrorCode;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ThumbnailImageStorage implements ThumbnailImageStorage {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${duckhu.storage.s3.bucket}")
  private String bucket;

  @Value("${duckhu.storage.s3.presigned-url-expiration:3600}")
  private long presignedUrlExpirationSeconds;

  // S3에 이미지를 저장하고 해당 파일의 key를 반환
  @Override
  public String upload(MultipartFile file) {
    String key = UUID.randomUUID().toString();

    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.getContentType())
            .build();

    try {
      s3Client.putObject(
          request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (IOException e) {
      throw new BookException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    return key; // 또는 get(key)로 URL 반환해도 됨
  }

  // 주어진 key에 대해 Presigned URL을 생성하여 반환
  // Presigned URL은 일시적으로 접근 가능한 S3 다운로드 링크
  @Override
  public String get(String key) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
            .getObjectRequest(getObjectRequest)
            .build();

    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

    log.info("[Presigned URL 생성] key: {}, url: {}", key, presignedRequest.url());

    return presignedRequest.url().toString();
  }

  // 주어진 URL에 해당하는 S3 객체를 삭제함
  @Override
  public void delete(String keyOrUrl) {
    String key = extractKeyFromUrl(keyOrUrl);

    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }

  // https://bucket.s3.region.amazonaws.com/key 형식에서 key만 추출
  private String extractKeyFromUrl(String url) {
    // 예: https://your-bucket.s3.ap-northeast-2.amazonaws.com/abc123.png
    URI uri = URI.create(url);
    return uri.getPath().substring(1); // 앞의 '/' 제거
  }
}
