package com.codeit.duckhu.domain.review.entity;

import com.codeit.duckhu.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
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
@Table(
    name = "review_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "user_id"}))
public class LikedUserId extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id")
  private Review review;


  @Column(name = "user_id", nullable = false)
  private UUID userId;

  public static LikedUserId of(Review review, UUID userId) {
    return LikedUserId.builder().review(review).userId(userId).build();
  }
}
