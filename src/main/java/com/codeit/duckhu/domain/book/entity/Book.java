package com.codeit.duckhu.domain.book.entity;

import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.global.entity.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Book extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String author;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private String publisher;

  @Column(name = "published_date", nullable = false)
  private LocalDate publishedDate;

  @Column(unique = true)
  private String isbn;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @Column(name = "review_count", nullable = false)
  @Builder.Default
  private Integer reviewCount = 0;

  @Column(name = "rating", nullable = false)
  @Builder.Default
  private Double rating = 0.0;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Builder.Default
  @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Review> reviews = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<PopularBook> popularBooks = new ArrayList<>();

  public void updateThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public void updateInfo(
      String title, String author, String description, String publisher, LocalDate publishedDate) {
    this.title = title;
    this.author = author;
    this.description = description;
    this.publisher = publisher;
    this.publishedDate = publishedDate;
  }

  public void logicallyDelete() {
    this.isDeleted = true;
  }

  public void updateReviewStatus(int reviewCount, double rating) {
    this.reviewCount = reviewCount;
    this.rating = rating;
  }
}
