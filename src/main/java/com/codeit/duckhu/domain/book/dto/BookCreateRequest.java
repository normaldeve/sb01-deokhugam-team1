package com.codeit.duckhu.domain.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * @param title 도서 제목 *
 * @param author 저자 *
 * @param description 설명
 * @param publisher 출판사 *
 * @param publishedDate 출판일 *
 * @param isbn ISBN
 */
public record BookCreateRequest(
    @NotBlank(message = "제목은 필수 입력값입니다.") String title,
    @NotBlank(message = "저자는 필수 입력값입니다.") String author,
    String description,
    @NotBlank(message = "출판사는 필수 입력값입니다") String publisher,
    @NotNull(message = "출판일은 필수 입력값입니다.") LocalDate publishedDate,
    @Pattern(
            regexp = "^(?=(?:\\D*\\d){10}(?:(?:\\D*\\d){3})?$)[\\d-]+$",
            message = "올바른 ISBN 형식이 아닙니다.")
        String isbn) {}
