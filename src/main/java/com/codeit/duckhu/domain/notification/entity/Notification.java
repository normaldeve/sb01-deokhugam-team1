package com.codeit.duckhu.domain.notification.entity;

import com.codeit.duckhu.global.entity.BaseUpdatableEntity;
import com.codeit.duckhu.global.type.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdatableEntity {

  @Column(name = "review_id", columnDefinition = "UUID")
  private UUID reviewId;

  @Column(name = "user_id", columnDefinition = "UUID")
  private UUID receiverId;

  @Column(name = "review_title", length = 255)
  private String reviewTitle;

  @Column(name = "content", length = 100, nullable = false)
  private String content;

  @Column(name = "confirmed", nullable = false)
  private boolean confirmed = false;

  private Notification(UUID reviewId, UUID receiverId, String content, String reviewTitle) {
    this.reviewId = reviewId;
    this.receiverId = receiverId;
    this.content = content;
    this.reviewTitle = reviewTitle;
    this.confirmed = false;
  }

  public static Notification forLike(
      UUID reviewId, UUID receiverId, String nickname, String reviewTitle) {
    String content = String.format("[%s]님이 나의 리뷰를 좋아합니다.", nickname);
    return new Notification(reviewId, receiverId, content, reviewTitle);
  }

  public static Notification forComment(
      UUID reviewId, UUID receiverId, String nickname, String comment, String reviewTitle) {
    String content = String.format("[%s]님이 나의 리뷰에 댓글을 남겼습니다.\n%s", nickname, comment);
    return new Notification(reviewId, receiverId, content, reviewTitle);
  }

  public static Notification forPopularReview(
      UUID reviewId, UUID receiverId, PeriodType period, int rank, String reviewTitle
  ) {
    String periodName;
    switch (period) {
      case DAILY:    periodName = "일간"; break;
      case WEEKLY:   periodName = "주간"; break;
      case MONTHLY:  periodName = "월간"; break;
      case ALL_TIME: periodName = "전체"; break;
      default:       periodName = period.name();
    }

    String content = String.format("나의 리뷰가 %s 인기 리뷰 %d위에 선정되었습니다.", periodName, rank);

    return new Notification(reviewId, receiverId, content, reviewTitle);
  }

  public void markAsConfirmed() {
    this.confirmed = true;
  }
}
