package com.codeit.duckhu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/** AWS S3 관련 설정을 구성하는 Configuration 클래스입니다. S3Client와 S3Presigner Bean을 생성하여 DI(의존성 주입)에 사용됩니다. */
@Configuration
public class S3Config {

  /** AWS S3 Access Key (application.yml 또는 환경변수로 주입됨) */
  @Value("${duckhu.storage.s3.access-key}")
  private String accessKey;

  /** AWS S3 Secret Key (application.yml 또는 환경변수로 주입됨) */
  @Value("${duckhu.storage.s3.secret-key}")
  private String secretKey;

  /** AWS S3 리전 (예: ap-northeast-2) */
  @Value("${duckhu.storage.s3.region}")
  private String region;

  /** S3Client Bean 생성 - 일반적인 S3 작업(업로드, 다운로드 등)을 수행하기 위한 클라이언트 */
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }

  /** S3Presigner Bean 생성 - Presigned URL을 생성할 때 사용하는 클라이언트 */
  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();
  }
}
