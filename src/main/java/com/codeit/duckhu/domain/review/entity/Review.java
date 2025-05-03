package com.codeit.duckhu.domain.review.entity;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.comment.domain.Comment;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.global.entity.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews")
public class Review extends BaseUpdatableEntity {

  @Column(name = "content", nullable = false)
  @NotBlank
  private String content;

  @Column(name = "rating", nullable = false)
  @Min(1)
  @Max(5)
  @NotNull
  private int rating;

  @Column(name = "like_count", nullable = false)
  @Builder.Default
  private int likeCount = 0;

  @Column(name = "comment_count", nullable = false)
  @Builder.Default
  private int commentCount = 0;

  @Column(name = "is_deleted", nullable = false)
  @Builder.Default
  private boolean isDeleted = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Book book;

  @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<LikedUserId> likedUserIds = new ArrayList<>();

  @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE, orphanRemoval = true)
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  // @Version private Long version;

  public void updateContent(String content) {
    this.content = content;
  }

  public void updateRating(int rating) {
    this.rating = rating;
  }

  public void increaseLikeCount(UUID userId) {
    if (!liked(userId)) {
      this.likedUserIds.add(LikedUserId.of(this, userId));
      this.likeCount++;
    }
  }

  public void decreaseLikeCount(UUID userId) {
    List<LikedUserId> toRemove =
        this.likedUserIds.stream()
            .filter(like -> like.getUserId().equals(userId))
            .collect(Collectors.toList());

    if (!toRemove.isEmpty()) {
      this.likedUserIds.removeAll(toRemove);
      if (this.likeCount > 0) {
        this.likeCount--;
      }
    }
  }

  public boolean liked(UUID userId) {
    return this.likedUserIds.stream().anyMatch(like -> like.getUserId().equals(userId));
  }

  public boolean toggleLike(UUID userId) {
    if (liked(userId)) {
      decreaseLikeCount(userId);
      return false;
    } else {
      increaseLikeCount(userId);
      return true;
    }
  }

  public void softDelete() {
    if (!this.isDeleted) {
      this.isDeleted = true;
    }
  }

  public void restore() {
    if (this.isDeleted) {
      this.isDeleted = false;
    }
  }

  public void increaseCommentCount() {
    this.commentCount++;
  }

  public void decreaseCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }

  public void updateCommentCount(int count) {
    this.commentCount = Math.max(0, count);
  }
}
