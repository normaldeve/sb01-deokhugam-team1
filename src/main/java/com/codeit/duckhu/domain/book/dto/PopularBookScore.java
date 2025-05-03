package com.codeit.duckhu.domain.book.dto;

import com.codeit.duckhu.domain.book.entity.Book;

public record PopularBookScore(Book book, int reviewCount, double rating, double score) {}
