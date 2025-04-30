package com.codeit.duckhu.domain.book.dto;

import java.time.LocalDate;

public record BookUpdateRequest(
    String title, String author, String description, String publisher, LocalDate publishedDate) {}
