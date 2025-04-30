package com.codeit.duckhu.domain.user.entity;

import com.codeit.duckhu.global.entity.BaseEntity;
import com.codeit.duckhu.global.type.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "power_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PowerUser extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "user_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_poweruser_user"))
  private User user;

  @Column(name = "review_score_sum", nullable = false)
  private Double reviewScoreSum;

  @Column(name = "like_count", nullable = false)
  private Integer likeCount;

  @Column(name = "comment_count", nullable = false)
  private Integer commentCount;

  @Column private Double score;

  @Column private Integer rank;

  @Enumerated(EnumType.STRING)
  @Column
  private PeriodType period;

  public void setRank(Integer rank) {
    this.rank = rank;
  }
}
