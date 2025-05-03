package com.codeit.duckhu.domain.book.naver;

import com.codeit.duckhu.domain.book.exception.BookException;
import com.codeit.duckhu.domain.book.exception.NaverAPIException;
import com.codeit.duckhu.global.exception.ErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class ImageConverterImpl implements ImageConverter {

  @Override
  public String convertToBase64(String imageUrl) {
    try (InputStream in = new URL(imageUrl).openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        baos.write(buffer, 0, bytesRead);
      }

      byte[] imageBytes = baos.toByteArray();
      return Base64.getEncoder().encodeToString(imageBytes);

    } catch (Exception e) {
      throw new NaverAPIException(ErrorCode.IMAGE_BASE64_CONVERSION_FAIL);
    }
  }
}
