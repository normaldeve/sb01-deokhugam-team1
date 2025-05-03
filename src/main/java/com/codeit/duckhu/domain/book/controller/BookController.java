package com.codeit.duckhu.domain.book.controller;

import com.codeit.duckhu.domain.book.controller.api.BookApi;
import com.codeit.duckhu.domain.book.dto.BookCreateRequest;
import com.codeit.duckhu.domain.book.dto.BookDto;
import com.codeit.duckhu.domain.book.dto.BookUpdateRequest;
import com.codeit.duckhu.domain.book.dto.CursorPageResponseBookDto;
import com.codeit.duckhu.domain.book.dto.CursorPageResponsePopularBookDto;
import com.codeit.duckhu.domain.book.dto.NaverBookDto;
import com.codeit.duckhu.domain.book.service.BookService;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController implements BookApi {

  private final BookService bookService;

  /**
   * 검색 조건에 맞는 도서 목록을 조회합니다.
   *
   * @param keyword 키워드를 통해 검색이 가능합니다. -> 도서 제목, 저자, ISBN
   * @param orderBy 정렬 기준 -> title, publishedDate, rating, reviewCount
   * @param direction 정렬 방향 -> DESC, ASC
   * @param cursor -> 커서 페이지네이션 커서
   * @param after -> 보조 커서(createdAt)
   * @param limit -> 페이지 크기
   * @return
   */
  @GetMapping
  public ResponseEntity<CursorPageResponseBookDto> getBooks(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "orderBy", defaultValue = "title") String orderBy,
      @RequestParam(value = "direction", defaultValue = "DESC") Direction direction,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "after", required = false) Instant after,
      @RequestParam(value = "limit", defaultValue = "50") int limit) {

    log.info("[도서 목록 조회 요청] keyword: {}, orderBy: {}, direction: {}, limit: {}", keyword, orderBy, direction, limit);

    return ResponseEntity.ok(
        bookService.searchBooks(keyword, orderBy, direction, cursor, after, limit));
  }

  /**
   * 도서 ID로 상세 정보를 조회합니다
   *
   * @param bookId 도서 ID
   * @return
   */
  @GetMapping(value = "/{bookId}")
  public ResponseEntity<BookDto> getBookById(
      @PathVariable(name = "bookId", required = true) UUID bookId) {

    log.info("[도서 상세 조회 요청] ID: {}", bookId);

    BookDto findBook = bookService.getBookById(bookId);

    return ResponseEntity.ok(findBook);
  }

  /**
   * 기간별 인기 도서 목록을 조회합니다.
   *
   * @param period 랭킹 기간 -> DAILY, WEEKLY, MONTHLY, ALL_TIME
   * @param direction 정렬 방향 -> DESC, ASC
   * @param cursor 커서 페이지네이션 커서
   * @param after 보조 커서 (createdAt)
   * @param limit 페이지 크기
   * @return
   */
  @GetMapping(value = "/popular")
  public ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
      @RequestParam(value = "period", defaultValue = "DAILY") PeriodType period,
      @RequestParam(value = "direction", defaultValue = "ASC") Direction direction,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "after", required = false) Instant after,
      @RequestParam(value = "limit", defaultValue = "50") int limit) {

    log.info("[인기 도서 목록 조회 요청] period: {}, direction: {}, limit: {}", period, direction, limit);

    return ResponseEntity.ok(
        bookService.searchPopularBooks(period, direction, cursor, after, limit));
  }

  /**
   * Naver API를 통해 ISBN으로 도서 정보를 조회합니다
   *
   * @param isbn
   * @return
   */
  @GetMapping("/info")
  public ResponseEntity<NaverBookDto> getBookByIsbn(@RequestParam("isbn") String isbn) {

    log.info("[Naver API 도서 조회 요청] ISBN: {}", isbn);

    return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
  }

  /**
   * 도서 정보를 수정합니다.
   *
   * @param bookId 도서 ID
   * @param bookData 수정할 도서 정보
   * @param thumbnailImage 수정할 도서 썸네일 이미지
   * @return
   */
  @PatchMapping(
      value = "/{bookId}",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<BookDto> updateBook(
      @PathVariable("bookId") UUID bookId,
      @RequestPart("bookData") BookUpdateRequest bookData,
      @RequestParam(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {

    log.info("[도서 수정 요청] ID: {}, 제목: {}", bookId, bookData.title());

    BookDto updateBook =
        bookService.updateBook(bookId, bookData, Optional.ofNullable(thumbnailImage));

    return ResponseEntity.ok(updateBook);
  }

  /**
   * 새로운 도서를 등록합니다.
   *
   * @param bookData 도서 정보
   * @param thumbnailImage 도서 썸네일 이미지
   * @return
   */
  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<BookDto> createBook(
      @Valid @RequestPart("bookData") BookCreateRequest bookData,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {

    log.info("[도서 등록 요청] 제목: {}, ISBN: {}", bookData.title(), bookData.isbn());

    BookDto createBook = bookService.registerBook(bookData, Optional.ofNullable(thumbnailImage));

    return ResponseEntity.status(HttpStatus.CREATED).body(createBook);
  }

  /**
   * 도서 이미지를 통해 ISBN을 인식합니다.
   *
   * @param image
   * @return
   */
  @PostMapping(
      value = "/isbn/ocr",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<String> extractIsbnOcr(@RequestPart(value = "image") MultipartFile image) {

    log.info("[OCR 요청] 이미지 파일명: {}, 크기: {} bytes", image.getOriginalFilename(), image.getSize());

    String isbn = bookService.extractIsbnFromImage(image);

    return ResponseEntity.ok(isbn);
  }

  /**
   * 도서를 논리적으로 삭제합니다. (관련된 댓글과 리뷰는 삭제하지 않음)
   *
   * @param bookId
   * @return
   */
  @DeleteMapping(value = "/{bookId}")
  public ResponseEntity<Void> deleteBookLogically(@PathVariable("bookId") UUID bookId) {

    log.info("[도서 논리 삭제 요청] ID: {}", bookId);

    bookService.deleteBookLogically(bookId);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  /**
   * 도서를 물리적으로 삭제합니다. (관련된 리뷰와 댓글 모두 삭제)
   *
   * @param bookId
   * @return
   */
  @DeleteMapping(value = "/{bookId}/hard")
  public ResponseEntity<Void> deleteBookPhysically(@PathVariable("bookId") UUID bookId) {

    log.info("[도서 물리 삭제 요청] ID: {}", bookId);

    bookService.deleteBookPhysically(bookId);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
