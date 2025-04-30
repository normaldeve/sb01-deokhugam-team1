package com.codeit.duckhu.domain.book.service;

import com.codeit.duckhu.domain.book.dto.BookDto;
import java.time.Instant;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.duckhu.domain.book.dto.BookCreateRequest;
import com.codeit.duckhu.domain.book.dto.BookUpdateRequest;
import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.exception.BookException;
import com.codeit.duckhu.domain.book.mapper.BookMapper;
import com.codeit.duckhu.domain.book.naver.NaverBookClient;
import com.codeit.duckhu.domain.book.ocr.OcrExtractor;
import com.codeit.duckhu.domain.book.repository.BookRepository;
import com.codeit.duckhu.domain.book.storage.ThumbnailImageStorage;
import com.codeit.duckhu.domain.review.repository.ReviewRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

  @Mock private BookRepository bookRepository;

  @Mock private BookMapper bookMapper;

  @Mock private ThumbnailImageStorage thumbnailImageStorage;

  @InjectMocks private BookServiceImpl bookService;

  @Nested
  @DisplayName("도서 저장")
  class SaveBookTest {

        @Test
        @DisplayName("도서 등록 성공 - 썸네일 있음")
        void registerBook_withThumbnail() {
          // given
          BookCreateRequest request = new BookCreateRequest(
              "Effective Java", "Joshua Bloch", "Best practices", "Addison-Wesley",
              LocalDate.of(2018, 1, 1), "9780134685991"
          );

          MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg",
              "image-content".getBytes());
          String thumbnailUrl = "https://s3.com/image.jpg";

          Book savedBook = Book.builder()
              .title(request.title())
              .author(request.author())
              .description(request.description())
              .publisher(request.publisher())
              .publishedDate(request.publishedDate())
              .isbn(request.isbn())
              .thumbnailUrl(thumbnailUrl)
              .build();

          BookDto expectedDto = new BookDto(
              UUID.randomUUID(), request.title(), request.author(), request.description(),
              request.publisher(), request.publishedDate(), request.isbn(),
              thumbnailUrl, 0, 0.0, Instant.now(), Instant.now()
          );

          given(bookRepository.existsByIsbn(request.isbn())).willReturn(false);
          given(thumbnailImageStorage.upload(file)).willReturn(thumbnailUrl);
          given(bookRepository.save(any(Book.class))).willReturn(savedBook);
          given(bookMapper.toDto(any(Book.class), any())).willReturn(expectedDto);

          // when
          BookDto result = bookService.registerBook(request, Optional.of(file));

          // then
          assertThat(result).isEqualTo(expectedDto);
          verify(bookRepository).save(any(Book.class));
          verify(thumbnailImageStorage).upload(file);
        }

        @Test
        @DisplayName("도서 등록 성공 - 썸네일 없음")
        void registerBook_withoutThumbnail_success() {
          // given
          BookCreateRequest request = new BookCreateRequest(
              "Clean Code", "Robert C. Martin", "Clean coding principles", "Prentice Hall",
              LocalDate.of(2008, 8, 1), "9780132350884"
          );

          Book savedBook = Book.builder()
              .title(request.title())
              .author(request.author())
              .description(request.description())
              .publisher(request.publisher())
              .publishedDate(request.publishedDate())
              .isbn(request.isbn())
              .thumbnailUrl(null)
              .build();

          BookDto expectedDto = new BookDto(
              UUID.randomUUID(), request.title(), request.author(), request.description(),
              request.publisher(), request.publishedDate(), request.isbn(),
              null, 0, 0.0, Instant.now(), Instant.now()
          );

          given(bookRepository.existsByIsbn(request.isbn())).willReturn(false);
          given(bookRepository.save(any(Book.class))).willReturn(savedBook);
          given(bookMapper.toDto(any(Book.class), any())).willReturn(expectedDto);

          // when
          BookDto result = bookService.registerBook(request, Optional.empty());

          // then
          assertThat(result).isEqualTo(expectedDto);
          verify(bookRepository).save(any(Book.class));
          verify(thumbnailImageStorage, never()).upload(any());
        }

    @Test
    @DisplayName("ISBN 중복 시 예외 발생")
    void registerBook_duplicateIsbn_exceptionThrown() {
      // given
      BookCreateRequest request =
          new BookCreateRequest(
              "토비 스프링", "토비", "스프링의 교과서", "Test", LocalDate.of(2003, 8, 30), "9780321125217");

      given(bookRepository.existsByIsbn(request.isbn())).willReturn(true);

      // when & then
      assertThatThrownBy(() -> bookService.registerBook(request, Optional.empty()))
          .isInstanceOf(BookException.class);

      verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("ISBN 형식에 맞지 않으면 예외 발생")
    void registerBook_invalidIsbn_exceptionThrown() {
      // given
      BookCreateRequest request =
          new BookCreateRequest(
              "Invalid Book",
              "Author",
              "Invalid ISBN format",
              "Publisher",
              LocalDate.of(2020, 1, 1),
              "INVALID_ISBN");

      // when & then
      assertThatThrownBy(() -> bookService.registerBook(request, Optional.empty()))
          .isInstanceOf(BookException.class);

      verify(bookRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("도서 조회")
  class GetBookTest {}

  @Nested
  @DisplayName("도서 업데이트")
  class UpdateBookTest {

        @Test
        @DisplayName("도서 정보와 썸네일을 수정하면 BookDto가 반환된다.")
        void updateBook_withThumbnailImage_success() {
          // Given
          UUID bookId = UUID.randomUUID();
          Book originalBook = Book.builder()
              .title("Old Title")
              .author("Old Author")
              .description("Old Desc")
              .publisher("Old Publisher")
              .publishedDate(LocalDate.of(2000, 1, 1))
              .isDeleted(false)
              .build();
          ReflectionTestUtils.setField(originalBook, "id", bookId);

          BookUpdateRequest request = new BookUpdateRequest(
              "New Title", "New Author", "New Desc", "New Publisher", LocalDate.of(2020, 5, 5)
          );

          MultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.jpg",
     "image/jpeg",
              "fake".getBytes());
          String uploadedUrl = "https://s3.bucket/thumbnail.jpg";

          int reviewCount = 5;
          double rating = 4.2;
          BookDto expectedDto = new BookDto(bookId, "New Title", "New Author", "New Desc",
              "New Publisher",
              LocalDate.now(), null, uploadedUrl, reviewCount, rating, Instant.now(),
     Instant.now());

          given(bookRepository.findById(bookId)).willReturn(Optional.of(originalBook));
          given(thumbnailImageStorage.upload(thumbnail)).willReturn(uploadedUrl);
          given(bookMapper.toDto(eq(originalBook), any())).willReturn(expectedDto);

          // When
          BookDto result = bookService.updateBook(bookId, request, Optional.of(thumbnail));

          // Then
          assertThat(result).isEqualTo(expectedDto);
          assertThat(originalBook.getTitle()).isEqualTo("New Title");
          assertThat(originalBook.getThumbnailUrl()).isEqualTo(uploadedUrl);
        }

    @Test
    @DisplayName("도서 수정 실패 - 존재하지 않는 도서")
    void updateBook_bookNotFound_throwsException() {
      // Given
      UUID bookId = UUID.randomUUID();
      BookUpdateRequest request =
          new BookUpdateRequest(
              "New Title", "New Author", "New Desc", "New Publisher", LocalDate.of(2020, 5, 5));

      given(bookRepository.findById(bookId)).willReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> bookService.updateBook(bookId, request, Optional.empty()))
          .isInstanceOf(BookException.class);
    }

    @Test
    @DisplayName("도서 수정 실패 - 논리 삭제된 도서")
    void updateBook_bookIsDeleted_throwsException() {
      // Given
      UUID bookId = UUID.randomUUID();
      Book deletedBook =
          Book.builder()
              .title("Old Title")
              .author("Author")
              .description("Desc")
              .publisher("Publisher")
              .publishedDate(LocalDate.of(2010, 1, 1))
              .isDeleted(true) // 논리 삭제된 상태
              .build();

      ReflectionTestUtils.setField(deletedBook, "id", bookId);

      BookUpdateRequest request =
          new BookUpdateRequest(
              "New Title", "New Author", "New Desc", "New Publisher", LocalDate.of(2020, 5, 5));

      given(bookRepository.findById(bookId)).willReturn(Optional.of(deletedBook));

      // When & Then
      assertThatThrownBy(() -> bookService.updateBook(bookId, request, Optional.empty()))
          .isInstanceOf(BookException.class);
    }
  }

  @Nested
  @DisplayName("도서 삭제")
  class DeleteBookTest {
    @Test
    @DisplayName("도서 논리 삭제 성공")
    void deleteBookLogically_success() {
      // Given
      UUID bookId = UUID.randomUUID();
      Book bookToDelete =
          Book.builder()
              .title("삭제할 책")
              .author("작가")
              .description("설명")
              .publisher("출판사")
              .publishedDate(LocalDate.of(2020, 1, 1))
              .isDeleted(false)
              .build();

      ReflectionTestUtils.setField(bookToDelete, "id", bookId);
      given(bookRepository.findById(bookId)).willReturn(Optional.of(bookToDelete));

      // When
      bookService.deleteBookLogically(bookId);

      // Then
      assertThat(bookToDelete.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("도서 논리 삭제 실패 - 존재하지 않는 책")
    void deleteBookLogically_notFound_throwsException() {
      // Given
      UUID invalidId = UUID.randomUUID();
      given(bookRepository.findById(invalidId)).willReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> bookService.deleteBookLogically(invalidId))
          .isInstanceOf(BookException.class);
    }

    @Test
    @DisplayName("도서 물리 삭제 성공")
    void deleteBookPhysically_success() {
      // Given
      UUID bookId = UUID.randomUUID();
      Book bookToDelete =
          Book.builder()
              .title("물리삭제 책")
              .author("작가")
              .description("설명")
              .publisher("출판사")
              .publishedDate(LocalDate.of(2020, 1, 1))
              .build();

      ReflectionTestUtils.setField(bookToDelete, "id", bookId);
      given(bookRepository.findById(bookId)).willReturn(Optional.of(bookToDelete));

      // When
      bookService.deleteBookPhysically(bookId);

      // Then
      verify(bookRepository).delete(bookToDelete);
    }

    @Test
    @DisplayName("도서 물리 삭제 실패 - 존재하지 않는 책")
    void deleteBookPhysically_notFound_throwsException() {
      // Given
      UUID invalidId = UUID.randomUUID();
      given(bookRepository.findById(invalidId)).willReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> bookService.deleteBookPhysically(invalidId))
          .isInstanceOf(BookException.class);
    }
  }
}
