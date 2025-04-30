package com.codeit.duckhu.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

  @NotNull private UUID id;

  @NotNull private UUID userId;

  @NotNull private UUID bookId;

  @NotNull private String bookTitle;

  @NotNull private String bookThumbnailUrl;

  @NotNull private String userNickname;

  @NotBlank private String content;

  @NotNull private int rating;

  @NotNull private int likeCount;

  @NotNull private int commentCount;

  private boolean likedByMe;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
