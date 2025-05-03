package com.codeit.duckhu.domain.book.naver;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.codeit.duckhu.domain.book.dto.NaverBookDto;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Naver API를 통해 도서 정보를 가져오는 통합 테스트를 진행합니다. */
@ActiveProfiles("test")
@SpringBootTest
@Import(NaverBookClientIntegrationTest.TestImageConverterConfig.class) // 테스트 전용 클래스를 Import 합니다.
class NaverBookClientIntegrationTest {

  @Autowired private NaverBookClient naverBookClient;

  @TestConfiguration
  static class TestImageConverterConfig {
    @Bean
    public ImageConverter imageConverter() {
      return imageUrl -> "dummyBase64Image"; // 썸네일 이미지를 테스트용 문자열로 고정합니다.
    }
  }

  @Test
  @DisplayName("실제 환경에서 Naver API를 통해서 도서 정보를 가져온다.")
  void searchBookByIsbn_realRestTemplate_fakeImageConverter() {
    // given
    String isbn = "9788936434120"; // 소년이 온다 도서 ISBN

    // when
    NaverBookDto book = naverBookClient.searchByIsbn(isbn);

    // then
    assertThat(book.title()).isNotBlank();
    assertThat(book.publisher()).isNotBlank();
    assertThat(book.thumbnailImage()).isEqualTo("dummyBase64Image");
    assertThat(book.publishedDate()).isInstanceOf(LocalDate.class);
  }
}
