package com.codeit.duckhu.domain.book.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component("tesseractOcrExtractor")
public class TesseractOcrExtractor implements OcrExtractor {

  @Override
  public String extractOCR(MultipartFile image) {
    try {
      log.info("OCR 요청: {}, size = {} bytes", image.getOriginalFilename(), image.getSize());
      BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
      if (bufferedImage == null) {
        return "0";
      }

      Tesseract tesseract = new Tesseract();
      File tessdataDir = new ClassPathResource("tessdata").getFile();
      tesseract.setDatapath(tessdataDir.getAbsolutePath());
      tesseract.setLanguage("eng+kor");

      String ocrResult = tesseract.doOCR(bufferedImage);
      log.info("OCR 결과: \n{}", ocrResult);

      return extractIsbnFromText(ocrResult);

    } catch (Exception e) {
      return "0";
    }
  }

  private String extractIsbnFromText(String text) {
    String cleaned = text.replaceAll("[\\n\\r]", " ").replaceAll("\\s+", " ");

    Pattern pattern =
        Pattern.compile(
            "ISBN[\\s:-]*((?:97[89][- ]?)?\\d{1,5}[- ]?\\d{1,7}[- ]?\\d{1,7}[- ]?\\d)",
            Pattern.CASE_INSENSITIVE);

    Matcher matcher = pattern.matcher(cleaned);
    if (matcher.find()) {
      String rawIsbn = matcher.group(1);
      return rawIsbn.replaceAll("[- ]", "");
    }

    return "0";
  }
}
