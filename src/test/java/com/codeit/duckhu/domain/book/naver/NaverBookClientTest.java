package com.codeit.duckhu.domain.book.naver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.codeit.duckhu.domain.book.dto.NaverBookDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("test")
@RestClientTest(NaverBookClient.class)
class NaverBookClientTest {

  @Autowired private NaverBookClient naverBookClient;

  @MockitoBean private ImageConverter imageConverter;

  @Autowired private ObjectMapper objectMapper;

  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    RestTemplate template = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(template);

    ReflectionTestUtils.setField(naverBookClient, "restTemplate", template);

    ReflectionTestUtils.setField(naverBookClient, "clientId", "fake-client-id");
    ReflectionTestUtils.setField(naverBookClient, "clientSecret", "fake-client-secret");
  }

  @Test
  @DisplayName("ISBN으로 도서를 조회할 수 있다")
  void searchByIsbn_success() {
    // given
    String isbn = "9788966261201";
    String responseBody =
        """
         {
           "items": [
             {
               "title": "클린 코드",
               "author": "로버트 마틴",
               "description": "좋은 코드에 대한 책",
               "publisher": "인사이트",
               "pubdate": "20131101",
               "isbn": "%s",
               "image": "https://dummy.com/image.jpg"
             }
           ]
         }
         """
            .formatted(isbn);

    mockServer
        .expect(requestTo("https://openapi.naver.com/v1/search/book_adv.json?d_isbn=" + isbn))
        .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    // 👇 썸네일 변환 부분은 실제 호출하지 않고 가짜 base64로 처리
    doReturn("mockBase64Image").when(imageConverter).convertToBase64(any());

    // when
    NaverBookDto book = naverBookClient.searchByIsbn(isbn);

    // then
    assertThat(book.title()).isEqualTo("클린 코드");
    assertThat(book.thumbnailImage()).isEqualTo("mockBase64Image");
  }
}
