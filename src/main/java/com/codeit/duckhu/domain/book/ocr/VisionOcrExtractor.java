package com.codeit.duckhu.domain.book.ocr;

import com.codeit.duckhu.domain.book.exception.OCRException;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component("visionOcrExtractor")
@Primary
public class VisionOcrExtractor implements OcrExtractor {

  private static final String DEFAULT_RESULT = "0";
  private static final String CREDENTIALS_PATH = "vision-key.json";
  private static final Pattern ISBN_PATTERN =
      Pattern.compile(
          "ISBN[\\s:-]*((?:97[89][- ]?)?\\d{1,5}[- ]?\\d{1,7}[- ]?\\d{1,7}[- ]?\\d)",
          Pattern.CASE_INSENSITIVE);

  @Override
  public String extractOCR(MultipartFile image) {
    log.info("OCR 처리 시작 - 파일명: {}, 크기: {} bytes", image.getOriginalFilename(), image.getSize());
    try {
      ByteString imgBytes = ByteString.readFrom(image.getInputStream());
      Image img = Image.newBuilder().setContent(imgBytes).build();
      Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();

      AnnotateImageRequest request =
          AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(img).build();

      try (ImageAnnotatorClient client = createClient()) {
        List<AnnotateImageResponse> responses =
            client.batchAnnotateImages(List.of(request)).getResponsesList();

        if (responses.isEmpty() || responses.get(0).hasError()) {
          String errorMsg =
              responses.isEmpty() ? "응답 없음" : responses.get(0).getError().getMessage();
          log.warn("Vision API 오류: {}", errorMsg);
          return DEFAULT_RESULT;
        }

        String extractedText = responses.get(0).getFullTextAnnotation().getText();
        log.debug("Vision API 텍스트 추출 완료");
        return extractIsbnFromText(extractedText);
      }

    } catch (IOException e) {
      log.error("이미지 스트림 읽기 실패 - 오류: {}", e.toString());
      throw new OCRException(ErrorCode.IMAGE_STREAM_READ_FAIL);
    } catch (Exception e) {
      log.error("Google Vision OCR 처리 - 오류:  {}", e.toString());
      throw new OCRException(ErrorCode.OCR_PROCESSING_FAIL);
    }
  }

  /** Vision API 클라이언트 생성 */
  private ImageAnnotatorClient createClient() {
    try {
      log.debug("Google Vision 클라이언트 생성 시도");
      GoogleCredentials credentials =
          GoogleCredentials.fromStream(new ClassPathResource(CREDENTIALS_PATH).getInputStream());

      ImageAnnotatorSettings settings =
          ImageAnnotatorSettings.newBuilder()
              .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
              .build();

      log.info("Google Vision 클라이언트 생성 성공");
      return ImageAnnotatorClient.create(settings);
    } catch (IOException e) {
      log.error("Google Vision 클라이언트 생성 실패", e);
      throw new OCRException(ErrorCode.GOOGLE_VISION_CLIENT_INIT_FAIL);
    }
  }

  /** OCR 결과 텍스트에서 ISBN 추출 */
  private String extractIsbnFromText(String text) {
    String cleanedText = text.replaceAll("[\\n\\r]", " ").replaceAll("\\s+", " ");
    log.debug("OCR 텍스트 정제 완료");
    Matcher matcher = ISBN_PATTERN.matcher(cleanedText);

    if (matcher.find()) {
      log.info("ISBN 추출 성공");
      return matcher.group(1).replaceAll("[- ]", "");
    }

    log.warn("ISBN 추출 실패 - OCR 결과: {}", cleanedText);
    throw new OCRException(ErrorCode.UNABLE_EXTRACT_ISBN);
  }
}
