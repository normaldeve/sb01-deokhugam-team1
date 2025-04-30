package com.codeit.duckhu.domain.book.controller;

import com.codeit.duckhu.domain.user.UserAuthenticationFilter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codeit.duckhu.domain.book.dto.BookCreateRequest;
import com.codeit.duckhu.domain.book.dto.BookDto;
import com.codeit.duckhu.domain.book.dto.BookUpdateRequest;
import com.codeit.duckhu.domain.book.dto.CursorPageResponseBookDto;
import com.codeit.duckhu.domain.book.dto.NaverBookDto;
import com.codeit.duckhu.domain.book.service.BookService;
import com.codeit.duckhu.global.type.Direction;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = BookController.class,
    excludeFilters =
    @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = UserAuthenticationFilter.class))
class BookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BookService bookService;

  @Autowired private ObjectMapper objectMapper;

  @Nested
  @DisplayName("도서 등록")
  class RegisterBook {

    @Test
    @DisplayName("도서를 등록하면 201 상태코드와 BookDto가 반환된다.")
    void createBook_shouldReturn201AndBookDto() throws Exception {
      // given
      BookCreateRequest request =
          new BookCreateRequest("클린 코드", "로버트 마틴", "소개", "출판사", LocalDate.now(), "1234567890");

      MockMultipartFile bookDataPart =
          new MockMultipartFile(
              "bookData", null, "application/json", objectMapper.writeValueAsBytes(request));

      MockMultipartFile thumbnailPart =
          new MockMultipartFile(
              "thumbnailImage",
              "image.jpg",
              MediaType.IMAGE_JPEG_VALUE,
              "fake-image-content".getBytes());

      BookDto response =
          new BookDto(
              UUID.randomUUID(),
              "클린 코드",
              "로버트 마틴",
              "소개",
              "출판사",
              LocalDate.of(2020, 1, 1),
              "1234567890",
              null, // thumbnailUrl
              0, // reviewCount
              0.0, // rating
              Instant.now(), // createdAt
              Instant.now() // updatedAt
              );

      given(bookService.registerBook(request, Optional.of(thumbnailPart))).willReturn(response);

      // when & then
      mockMvc
          .perform(
              multipart("/api/books")
                  .file(bookDataPart)
                  .file(thumbnailPart)
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.title").value("클린 코드"))
          .andExpect(jsonPath("$.author").value("로버트 마틴"));
    }
  }

  @Nested
  @DisplayName("도서 조회")
  class GetBook {

    @Test
    @DisplayName("도서 목록 조회 시 결과가 반환된다.")
    void getBooks_shouldReturnBookList() throws Exception {
      // given
      CursorPageResponseBookDto fakeResponse =
          new CursorPageResponseBookDto(List.of(), null, null, 0, 0, false);

      given(bookService.searchBooks(null, "title", Direction.DESC, null, null, 50))
          .willReturn(fakeResponse);

      // when & then
      mockMvc.perform(get("/api/books")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("ISBN으로 도서 정보를 조회한다")
    void getBookByIsbn_shouldReturnBookInfo() throws Exception {
      // given
      String isbn = "9781234567890";
      NaverBookDto dto = new NaverBookDto("테스트 책", "저자", "테스트", "테스트", LocalDate.now(), null, null);

      given(bookService.getBookByIsbn(isbn)).willReturn(dto);

      // when & then
      mockMvc
          .perform(get("/api/books/info").param("isbn", isbn))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("테스트 책"))
          .andExpect(jsonPath("$.author").value("저자"));
    }
  }

  @Nested
  @DisplayName("도서 수정")
  class UpdateBook {
    @Test
    @DisplayName("도서를 수정하면 200 상태 코드와 BookDto가 반환된다.")
    void updateBook_shouldReturn200AndBookDto() throws Exception {
      UUID bookId = UUID.randomUUID();
      BookUpdateRequest updateRequest =
          new BookUpdateRequest("수정된 제목", "수정된 작가", "수정된 설명", "수정된 출판사", LocalDate.now());

      MockMultipartFile bookDataPart =
          new MockMultipartFile(
              "bookData", null, "application/json", objectMapper.writeValueAsBytes(updateRequest));
      MockMultipartFile thumbnailPart =
          new MockMultipartFile(
              "thumbnailImage",
              "image.jpg",
              MediaType.IMAGE_JPEG_VALUE,
              "fake-thumbnail-content".getBytes());

      BookDto response =
          new BookDto(
              bookId,
              "수정된 제목",
              "수정된 작가",
              "수정된 설명",
              "수정된 출판사",
              LocalDate.now(),
              "1234567890",
              "https://s3.com/thumb.jpg",
              3,
              4.5,
              Instant.now(),
              Instant.now());

      given(bookService.updateBook(eq(bookId), any(), any())).willReturn(response);

      mockMvc
          .perform(
              multipart("/api/books/{bookId}", bookId)
                  .file(bookDataPart)
                  .file(thumbnailPart)
                  .with(
                      req -> {
                        req.setMethod("PATCH");
                        return req;
                      })
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("수정된 제목"));
    }
  }

  @Nested
  @DisplayName("도서 삭제")
  class DeleteBook {
    @Test
    @DisplayName("도서를 논리 삭제하면 204 상태 코드가 반환된다.")
    void deleteBookLogically_shouldReturn204() throws Exception {
      UUID bookId = UUID.randomUUID();

      mockMvc.perform(delete("/api/books/{bookId}", bookId)).andExpect(status().isNoContent());

      verify(bookService).deleteBookLogically(bookId);
    }

    @Test
    @DisplayName("도서를 물리 삭제하면 204 상태 코드가 반환된다.")
    void deleteBookPhysically_shouldReturn204() throws Exception {
      UUID bookId = UUID.randomUUID();

      mockMvc.perform(delete("/api/books/{bookId}/hard", bookId)).andExpect(status().isNoContent());

      verify(bookService).deleteBookPhysically(bookId);
    }
  }

  @Test
  @DisplayName("OCR 이미지 업로드 시 ISBN을 추출한다")
  void extractIsbnFromImage_shouldReturnIsbn() throws Exception {
    // given
    MockMultipartFile image =
        new MockMultipartFile(
            "image", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-content".getBytes());

    given(bookService.extractIsbnFromImage(image)).willReturn("9781234567890");

    // when & then
    mockMvc
        .perform(
            multipart("/api/books/isbn/ocr").file(image).contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(content().string("9781234567890"));
  }
}
