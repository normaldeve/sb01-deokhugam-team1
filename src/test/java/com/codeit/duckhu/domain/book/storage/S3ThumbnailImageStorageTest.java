package com.codeit.duckhu.domain.book.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.duckhu.domain.book.storage.s3.S3ThumbnailImageStorage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3ThumbnailImageStorageTest {

  @Mock private S3Client s3Client; // S3에 실제로 연결되지 않고 동작을 시뮬레이션할 객체

  @Mock private S3Presigner s3Presigner; // Presigned URL 생성을 시뮬레이션할 객체

  @InjectMocks private S3ThumbnailImageStorage storage; // 테스트 대상 객체 (위 Mock들을 내부에 주입)

  @BeforeEach
  void setUp() {
    // ReflectionTestUtils를 통해 @Value로 주입되는 필드를 강제로 설정
    ReflectionTestUtils.setField(storage, "bucket", "test-bucket");
    ReflectionTestUtils.setField(storage, "presignedUrlExpirationSeconds", 3600L);
  }

  @Test
  @DisplayName("파일이 정상적으로 S3에 저장되어야 한다.")
  void upload_shouldStoreFileAndReturnKey() throws IOException {
    // given
    // 가상의 MultipartFile 생성
    MultipartFile mockFile = mock(MultipartFile.class);
    given(mockFile.getInputStream()).willReturn(new ByteArrayInputStream("test".getBytes()));
    given(mockFile.getSize()).willReturn(4L);
    given(mockFile.getContentType()).willReturn("image/jpeg");

    // when
    // upload() 실행 시 내부적으로 S3Client.putObject()가 호출됨
    String key = storage.upload(mockFile);

    // then
    // UUID 형식의 key가 반환되고, putObject가 한 번 호출되었는지 검증
    assertThat(key).isNotBlank();
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("Presigned URL을 반환받을 수 있다.")
  void get_shouldReturnPresignedUrl() throws MalformedURLException {
    // given
    // S3에 저장된 파일을 다운로드할 수 있는 URL을 테스트하기 위한 설정
    String key = "abc-123";
    String expectedUrl = "https://s3.amazonaws.com/test-bucket/" + key;

    // PresignedGetObjectRequest의 .url() 호출 시 예상 URL이 반환되도록 설정
    PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
    given(mockPresignedRequest.url()).willReturn(new URL(expectedUrl)); // URL로 변경

    // presignGetObject 호출 시 위의 PresignedGetObjectRequest 반환되도록 설정
    given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
        .willReturn(mockPresignedRequest);

    // when
    // get() 호출 → 내부적으로 presignGetObject() 호출됨
    String url = storage.get(key);

    // then
    // 반환된 URL이 예상 URL과 동일한지 검증
    assertThat(url).isEqualTo(expectedUrl);
  }

  @Test
  @DisplayName("URL을 통해 S3에 파일을 삭제할 수 있다")
  void delete_shouldCallS3DeleteObject() {
    // given
    // 테스트용 S3 객체 URL → 내부적으로 key 추출이 일어남
    String url = "https://test-bucket.s3.us-east-1.amazonaws.com/sample.jpg";

    // when
    // delete() 실행 시 deleteObject가 호출됨
    storage.delete(url);

    // then
    // deleteObject 메서드가 적절한 bucket과 key로 호출됐는지 검증
    verify(s3Client)
        .deleteObject(
            argThat(
                (DeleteObjectRequest request) ->
                    request.bucket().equals("test-bucket") && request.key().equals("sample.jpg")));
  }
}
