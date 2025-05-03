package com.codeit.duckhu.domain.book.service;

import com.codeit.duckhu.domain.book.dto.BookDto;
import com.codeit.duckhu.domain.book.dto.CursorPageResponseBookDto;
import com.codeit.duckhu.domain.book.dto.NaverBookDto;
import com.codeit.duckhu.domain.book.entity.PopularBook;
import com.codeit.duckhu.domain.book.exception.OCRException;
import com.codeit.duckhu.domain.book.mapper.PopularBookMapper;
import com.codeit.duckhu.domain.book.repository.popular.PopularBookRepository;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

  @Mock
  private BookRepository bookRepository;

  @Mock
  private NaverBookClient naverBookClient;

  @Mock
  private BookMapper bookMapper;

  @Mock
  private PopularBookMapper popularBookMapper;

  @Mock OcrExtractor ocrExtractor;

  @Mock
  private ThumbnailImageStorage thumbnailImageStorage;

  @Mock
  private PopularBookRepository popularBookRepository;

  @InjectMocks
  private BookServiceImpl bookService;

  /**
   * 도서를 등록하는 메서드를 검증하였습니다.
   * - 썸네일 이미지 유무에 따라 저장 로직이 다르게 동작합니다.
   * - ISBN 중복 여부, ISBN 형식 검증을 포함합니다.
   */
  @Nested
  @DisplayName("도서 저장")
  class SaveBookTest {

    @Test
    @DisplayName("도서 등록 성공 - 썸네일 있음")
    void registerBook_withThumbnail() {
      // given : 썸네일 이미지가 포함된 도서 등록 요청 세팅
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

      // when : 도서 등록 서비스 호출
      BookDto result = bookService.registerBook(request, Optional.of(file));

      // then : 정상적으로 저장 및 변환되었는지 호출
      assertThat(result).isEqualTo(expectedDto);
      verify(bookRepository).save(any(Book.class));
      verify(thumbnailImageStorage).upload(file);
    }

    @Test
    @DisplayName("도서 등록 성공 - 썸네일 없음")
    void registerBook_withoutThumbnail_success() {
      // given : 썸네일이 없는 도서 등록 요청 준비
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

      // then -> 썸네일 이미지 저장 메서드가 실행되지 않음을 검증
      assertThat(result).isEqualTo(expectedDto);
      verify(bookRepository).save(any(Book.class));
      verify(thumbnailImageStorage, never()).upload(any());
    }

    @Test
    @DisplayName("ISBN 중복 시 예외 발생")
    void registerBook_duplicateIsbn_exceptionThrown() {
      // given : 중복된 iSBN을 입력
      BookCreateRequest request =
          new BookCreateRequest(
              "토비 스프링", "토비", "스프링의 교과서", "Test", LocalDate.of(2003, 8, 30), "9780321125217");

      given(bookRepository.existsByIsbn(request.isbn())).willReturn(true);

      // when & then -> 예외가 발생하며 도서 레포지토리 저장 메서드가 실행되지 않음을 검증
      assertThatThrownBy(() -> bookService.registerBook(request, Optional.empty()))
          .isInstanceOf(BookException.class);

      verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("ISBN 형식에 맞지 않으면 예외 발생")
    void registerBook_invalidIsbn_exceptionThrown() {
      // given : ISBN 형식에 맞지 않는 도서 등록
      BookCreateRequest request =
          new BookCreateRequest(
              "Invalid Book",
              "Author",
              "Invalid ISBN format",
              "Publisher",
              LocalDate.of(2020, 1, 1),
              "INVALID_ISBN");

      // when & then -> 예외가 터지며 도서 레포지토리에 저장 메서드가 실행되지 않음
      assertThatThrownBy(() -> bookService.registerBook(request, Optional.empty()))
          .isInstanceOf(BookException.class);

      verify(bookRepository, never()).save(any());
    }
  }

  /**
   * 도서를 검색하는 메서드를 검증하였습니다.
   * - 키워드 검색, 정렬 기준에 따라 도서를 조회할 수 있습니다.
   * - 인기 도서(기간별) 검색 기능을 포함합니다.
   */
  @Nested
  @DisplayName("도서 조회")
  class GetBookTest {

    @Test
    @DisplayName("도서 목록 검색 성공")
    void searchBooks_success() {
      // Given
      UUID id = UUID.randomUUID();
      Book book = Book.builder()
          .title("테스트 도서")
          .author("작가")
          .publishedDate(LocalDate.of(2024, 1, 1))
          .rating(4.5)
          .reviewCount(10)
          .build();

      ReflectionTestUtils.setField(book, "id", id);
      ReflectionTestUtils.setField(book, "createdAt", Instant.now());
      ReflectionTestUtils.setField(book, "updatedAt", Instant.now());

      BookDto bookDto = new BookDto(
          book.getId(), book.getTitle(), book.getAuthor(), null, null,
          null, null, null, 10, 4.5, book.getCreatedAt(), book.getUpdatedAt()
      );

      given(bookRepository.searchBooks(any(), any(), any(), any(), any(), anyInt()))
          .willReturn(List.of(book));
      given(bookMapper.toDto(any(Book.class), any())).willReturn(bookDto);

      // When
      CursorPageResponseBookDto result = bookService.searchBooks("테스트", "title", Direction.DESC,
          null, Instant.now(), 10);

      // Then
      assertThat(result.content().size()).isEqualTo(1);
      assertThat(result.content().get(0).title()).isEqualTo("테스트 도서");
      assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("인기 도서 목록 검색 성공")
    void searchPopularBooks_success() {
      // Given
      UUID id = UUID.randomUUID();
      Book book = Book.builder()
          .title("인기 도서")
          .author("인기 작가")
          .publishedDate(LocalDate.of(2024, 2, 1))
          .rating(4.8)
          .reviewCount(100)
          .build();
      ReflectionTestUtils.setField(book, "id", id);
      ReflectionTestUtils.setField(book, "createdAt", Instant.now());
      ReflectionTestUtils.setField(book, "updatedAt", Instant.now());

      PopularBook popularBook = PopularBook.builder()
          .book(book)
          .rank(1)
          .period(com.codeit.duckhu.global.type.PeriodType.MONTHLY)
          .score(4.5)
          .build();
      ReflectionTestUtils.setField(popularBook, "id", id);
      ReflectionTestUtils.setField(popularBook, "createdAt", Instant.now());

      given(popularBookRepository.searchByPeriodWithCursorPaging(any(), any(), any(), any(),
          anyInt()))
          .willReturn(List.of(popularBook));
      given(popularBookRepository.countByPeriod(any())).willReturn(1);
      given(popularBookMapper.toDto(any(), any()))
          .willReturn(new com.codeit.duckhu.domain.book.dto.PopularBookDto(
              popularBook.getId(), book.getId(), book.getTitle(), book.getAuthor(), null,
              PeriodType.MONTHLY.toString(), popularBook.getRank(), popularBook.getScore(),
              book.getReviewCount(), book.getRating(), book.getCreatedAt()
          ));

      // When
      var result = bookService.searchPopularBooks(com.codeit.duckhu.global.type.PeriodType.MONTHLY,
          Direction.DESC, null, Instant.now(), 10);

      // Then
      assertThat(result.content().size()).isEqualTo(1);
      assertThat(result.content().get(0).rank()).isEqualTo(1);
      assertThat(result.totalElements()).isEqualTo(1);
      assertThat(result.hasNext()).isFalse();
    }
  }

  /**
   * 도서를 ID로 단건 조회하는 메서드를 검증하였습니다.
   * - ID가 존재하는 경우 BookDto를 반환합니다.
   * - 존재하지 않는 ID 입력 시 예외를 발생시킵니다.
   */
  @Nested
  @DisplayName("도서 단건 조회")
  class GetBookByIdTest {

    @Test
    @DisplayName("도서 ID를 통해 조회 성공")
    void getBookById_success() {
      // Given
      UUID id = UUID.randomUUID();
      Book book = Book.builder()
          .title("인기 도서")
          .author("인기 작가")
          .publishedDate(LocalDate.of(2024, 2, 1))
          .rating(4.8)
          .reviewCount(100)
          .thumbnailUrl("thumbnail-key")
          .build();
      ReflectionTestUtils.setField(book, "id", id);
      ReflectionTestUtils.setField(book, "createdAt", Instant.now());
      ReflectionTestUtils.setField(book, "updatedAt", Instant.now());

      BookDto expectedDto = new BookDto(
          book.getId(), book.getTitle(), book.getAuthor(), null, null,
          null, null, "https://s3.com/thumbnail.jpg", 20, 4.5,
          book.getCreatedAt(), book.getCreatedAt()
      );

      given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));
      given(thumbnailImageStorage.get("thumbnail-key")).willReturn("https://s3.com/thumbnail.jpg");
      given(bookMapper.toDto(book, "https://s3.com/thumbnail.jpg")).willReturn(expectedDto);

      // When
      BookDto result = bookService.getBookById(book.getId());

      // Then
      assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("도서 ID를 통해 조회 실패 - 존재하지 않는 ID")
    void getBookById_notFound_throwsException() {
      // Given
      UUID bookId = UUID.randomUUID();
      given(bookRepository.findById(bookId)).willReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> bookService.getBookById(bookId))
          .isInstanceOf(BookException.class);
    }
  }

  /**
   * ISBN을 이용하여 외부 API(Naver Book API)로 도서 정보를 조회하는 기능을 검증하였습니다.
   * - 정상 ISBN 입력 시 정보를 가져오고, 잘못된 형식이면 예외를 발생시킵니다.
   */
  @Nested
  @DisplayName("도서 ISBN 조회")
  class GetBookByIsbnTest {

    @Test
    @DisplayName("ISBN으로 도서 조회 성공")
    void getBookByIsbn_success() {
      // Given
      String validIsbn = "9780134685991";
      NaverBookDto naverBookDto = new NaverBookDto(
          "Effective Java", "Joshua Bloch", "Best practices", "Addison-Wesley", LocalDate.now(),
          validIsbn, "https://image.url"
      );

      given(naverBookClient.searchByIsbn(validIsbn)).willReturn(naverBookDto);

      // When
      NaverBookDto result = bookService.getBookByIsbn(validIsbn);

      // Then
      assertThat(result).isEqualTo(naverBookDto);
    }

    @Test
    @DisplayName("잘못된 ISBN으로 조회 시 예외 발생")
    void getBookByIsbn_invalidIsbn_throwsException() {
      // Given
      String invalidIsbn = "INVALID_ISBN";

      // When & Then
      assertThatThrownBy(() -> bookService.getBookByIsbn(invalidIsbn))
          .isInstanceOf(BookException.class)
          .hasMessageContaining("ISBN 형식");

      verify(naverBookClient, never()).searchByIsbn(any());
    }
  }

  /**
   * 도서 정보를 수정하는 기능을 검증하였습니다.
   * - 제목, 저자, 설명, 출판사, 출판일 등의 필드를 수정할 수 있습니다.
   * - 썸네일 이미지 변경도 가능합니다.
   * - 존재하지 않거나 논리 삭제된 도서에 대해서는 예외를 발생시킵니다.
   */
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

  /**
   * 이미지 파일에서 ISBN을 추출하는 기능을 검증하였습니다.
   * - 정상 이미지 파일이면 ISBN을 추출하고, 이미지가 아닐 경우 예외를 발생시킵니다.
   */
  @Nested
  @DisplayName("이미지에서 ISBN 추출")
  class ExtractIsbnFromImageTest {

    @Test
    @DisplayName("정상 이미지 파일로 ISBN 추출 성공")
    void extractIsbnFromImage_success() {
      // Given
      MultipartFile image = new MockMultipartFile(
          "file", "book.jpg", "image/jpeg", "fake image content".getBytes()
      );

      String extractedIsbn = "9780134685991";

      given(ocrExtractor.extractOCR(image)).willReturn(extractedIsbn);

      // When
      String result = bookService.extractIsbnFromImage(image);

      // Then
      assertThat(result).isEqualTo(extractedIsbn);
      verify(ocrExtractor).extractOCR(image); // OCR 호출했는지 검증
    }

    @Test
    @DisplayName("이미지 파일이 아닐 경우 예외 발생")
    void extractIsbnFromImage_invalidFile_throwsException() {
      // Given
      MultipartFile invalidFile = new MockMultipartFile(
          "file", "text.txt", "text/plain", "this is not an image".getBytes()
      );

      // When & Then
      assertThatThrownBy(() -> bookService.extractIsbnFromImage(invalidFile))
          .isInstanceOf(OCRException.class);

      verify(ocrExtractor, never()).extractOCR(any()); // OCRExtractor는 호출되면 안됨
    }
  }

  /**
   * 도서를 삭제하는 기능을 검증하였습니다.
   * - 논리 삭제: isDeleted를 true로 변경합니다.
   * - 물리 삭제: DB에서 도서를 제거하며, 썸네일 이미지도 함께 삭제합니다.
   * - 존재하지 않는 도서에 대해서는 예외를 발생시킵니다.
   */
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
    @DisplayName("도서 물리 삭제 성공 - 썸네일 삭제도 성공")
    void deleteBookPhysically_success_withThumbnail() {
      // Given
      UUID bookId = UUID.randomUUID();
      String thumbnailKey = "s3-thumbnail-key.jpg";

      Book bookToDelete = Book.builder()
          .title("물리삭제 책")
          .author("작가")
          .description("설명")
          .publisher("출판사")
          .publishedDate(LocalDate.of(2020, 1, 1))
          .thumbnailUrl(thumbnailKey)  // 썸네일 키 설정
          .build();

      ReflectionTestUtils.setField(bookToDelete, "id", bookId);

      given(bookRepository.findById(bookId)).willReturn(Optional.of(bookToDelete));

      // When
      bookService.deleteBookPhysically(bookId);

      // Then
      verify(thumbnailImageStorage).delete(thumbnailKey); // 썸네일 삭제 검증 추가
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
