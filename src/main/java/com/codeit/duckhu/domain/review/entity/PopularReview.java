package com.codeit.duckhu.domain.review.entity;

import com.codeit.duckhu.global.entity.BaseEntity;
import com.codeit.duckhu.global.type.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인기 리뷰 엔티티
 *
 * <p>좋아요 수와 댓글 수를 기반으로 계산된 점수와 랭킹 정보를 저장합니다. 기간별(일간, 주간, 월간 등)로 인기 리뷰를 관리합니다.
 */
@Entity
@Builder
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
    name = "popular_reviews",
    indexes = {
      @Index(name = "idx_popular_reviews_period_rank", columnList = "period, rank"),
      @Index(name = "idx_popular_reviews_period_created_at", columnList = "period, created_at")
    })
public class PopularReview extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "review_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_popularreview_review"))
  private Review review;

  @Column(name = "review_rating", nullable = false)
  private Double reviewRating;

  @Enumerated(EnumType.STRING)
  @Column(name = "period", nullable = false)
  private PeriodType period;

  @Column(name = "like_count", nullable = false)
  private Integer likeCount;

  @Column(name = "comment_count", nullable = false)
  private Integer commentCount;

  @Column(name = "score", nullable = false)
  private Double score;

  @Column(name = "rank", nullable = false)
  private Integer rank;

  public void setRank(Integer rank) {
    this.rank = rank;
  }
}
