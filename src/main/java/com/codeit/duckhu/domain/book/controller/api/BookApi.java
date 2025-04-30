package com.codeit.duckhu.domain.book.controller.api;

import com.codeit.duckhu.domain.book.dto.BookCreateRequest;
import com.codeit.duckhu.domain.book.dto.BookDto;
import com.codeit.duckhu.domain.book.dto.BookUpdateRequest;
import com.codeit.duckhu.domain.book.dto.CursorPageResponseBookDto;
import com.codeit.duckhu.domain.book.dto.CursorPageResponsePopularBookDto;
import com.codeit.duckhu.domain.book.dto.NaverBookDto;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
@RequestMapping("/api/books")
public interface BookApi {

  @Operation(summary = "도서 목록 조회", description = "검색 조건에 맞는 도서 목록을 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "도서 목록 조회 성공",
        content = @Content(schema = @Schema(implementation = CursorPageResponseBookDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
        content = @Content(schema = @Schema(implementation = CursorPageResponseBookDto.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = CursorPageResponseBookDto.class))),
  })
  @GetMapping
  ResponseEntity<CursorPageResponseBookDto> getBooks(
      @Parameter(description = "도서 제목 | 저자 | ISBN", example = "자바") @RequestParam(required = false)
          String keyword,
      @Parameter(
              description = "정렬 기준 (title | publishedDate | rating | reviewCount)",
              example = "title")
          @RequestParam(defaultValue = "title")
          String orderBy,
      @Parameter(description = "정렬 방향", example = "DESC") @RequestParam(defaultValue = "DESC")
          Direction direction,
      @Parameter(description = "커서 페이지네이션 커서") @RequestParam(required = false) String cursor,
      @Parameter(description = "보조 커서(createdAt)") @RequestParam(required = false) Instant after,
      @Parameter(description = "페이지 크기", example = "50") @RequestParam(defaultValue = "50")
          int limit);

  @Operation(summary = "도서 등록", description = "새로운 도서를 등록합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "도서 등록 성공",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (입력값 검증 실패, ISBN 형식 오류 등)",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "409",
        description = "ISBN 중복",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = BookDto.class)))
  })
  @PostMapping(consumes = {"multipart/form-data"})
  ResponseEntity<BookDto> createBook(
      @Parameter(name = "bookData", description = "도서 정보", required = true) @RequestPart("bookData")
          BookCreateRequest bookData,
      @Parameter(name = "thumbnailImage", description = "도서 썸네일 이미지")
          @RequestPart(value = "thumbnailImage", required = false)
          MultipartFile thumbnailImage);

  @Operation(summary = "이미지 기반 ISBN 인식", description = "도서 이미지를 통해 ISBN을 인식합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "ISBN 인식 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 이미지 형식 또는 OCR 인식 실패"),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping(
      value = "/isbn/ocr",
      consumes = {"multipart/form-data"})
  ResponseEntity<String> extractIsbnOcr(
      @Parameter(name = "image", description = "도서 이미지", required = true)
          @RequestPart(value = "image")
          MultipartFile image);

  @Operation(summary = "ISBN으로 도서 정보 조회", description = "Naver API를 통해 ISBN으로 도서 정보를 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "도서 정보 조회 성공",
        content = @Content(schema = @Schema(implementation = NaverBookDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 ISBN 형식",
        content = @Content(schema = @Schema(implementation = NaverBookDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "도서 정보 없음",
        content = @Content(schema = @Schema(implementation = NaverBookDto.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = NaverBookDto.class)))
  })
  @GetMapping("/info")
  ResponseEntity<NaverBookDto> getBookByIsbn(
      @Parameter(name = "isbn", description = "ISBN 번호", example = "9788965402602", required = true)
          @RequestParam(value = "isbn")
          String isbn);

  @Operation(summary = "도서 정보 수정", description = "도서 정보를 수정합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "도서 정보 수정 성공",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (입력값 검증 실패, ISBN 형식 오류 등)",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "도서 정보 없음",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "409",
        description = "ISBN 중복",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = BookDto.class)))
  })
  @PatchMapping(
      value = "/{bookId}",
      consumes = {"multipart/form-data"})
  ResponseEntity<BookDto> updateBook(
      @Parameter(
              name = "bookId",
              description = "도서 ID",
              example = "123e4567-e89b-12d3-a456-426614174000",
              required = true)
          @PathVariable(value = "bookId")
          UUID bookId,
      @Parameter(name = "bookData", description = "수정할 도서 정보", required = true)
          @RequestPart("bookData")
          BookUpdateRequest bookData,
      @Parameter(name = "thumbnailImage", description = "수정할 도서 썸네일 이미지")
          @RequestPart(value = "thumbnailImage", required = false)
          MultipartFile thumbnailImage);

  @Operation(summary = "인기 도서 목록 조회", description = "기간별 인기 도서 목록을 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "인기 도서 목록 조회 성공",
        content =
            @Content(schema = @Schema(implementation = CursorPageResponsePopularBookDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)",
        content =
            @Content(schema = @Schema(implementation = CursorPageResponsePopularBookDto.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content =
            @Content(schema = @Schema(implementation = CursorPageResponsePopularBookDto.class)))
  })
  @GetMapping(value = "/popular")
  ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
      @Parameter(name = "period", description = "랭킹 기간", example = "DAILY")
          @RequestParam(value = "period", defaultValue = "DAILY")
          PeriodType period,
      @Parameter(name = "direction", description = "정렬 방향", example = "DESC")
          @RequestParam(value = "direction", defaultValue = "ASC")
          Direction direction,
      @Parameter(name = "cursor", description = "커서 페이지네이션 커서")
          @RequestParam(value = "cursor", required = false)
          String cursor,
      @Parameter(name = "after", description = "보조 커서(createdAt)")
          @RequestParam(value = "after", required = false)
          Instant after,
      @Parameter(name = "limit", description = "페이지 크기", example = "50")
          @RequestParam(value = "limit", defaultValue = "50")
          int limit);

  @Operation(summary = "도서 논리 삭제", description = "도서를 논리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
    @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping(value = "/{bookId}")
  ResponseEntity<Void> deleteBookLogically(
      @Parameter(
              name = "bookId",
              description = "도서 ID",
              example = "123e4567-e89b-12d3-a456-426614174000",
              required = true)
          @PathVariable("bookId")
          UUID bookId);

  @Operation(summary = "도서 물리 삭제", description = "도서를 물리적으로 삭제합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
    @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping(value = "/{bookId}/hard")
  ResponseEntity<Void> deleteBookPhysically(
      @Parameter(
              name = "bookId",
              description = "도서 ID",
              example = "123e4567-e89b-12d3-a456-426614174000",
              required = true)
          @PathVariable("bookId")
          UUID bookId);

  @Operation(summary = "도서 상세 정보 조회", description = "도서 ID로 상세 정보를 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "도서 정보 조회 성공",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "도서 정보 없음",
        content = @Content(schema = @Schema(implementation = BookDto.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = @Content(schema = @Schema(implementation = BookDto.class)))
  })
  @GetMapping(value = "/{bookId}")
  ResponseEntity<BookDto> getBookById(
      @Parameter(
              name = "bookId",
              description = "도서 ID",
              example = "123e4567-e89b-12d3-a456-426614174000")
          @PathVariable(name = "bookId", required = true)
          UUID bookId);
}
