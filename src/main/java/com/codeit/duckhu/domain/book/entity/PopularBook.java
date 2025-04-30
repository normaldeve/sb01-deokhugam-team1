package com.codeit.duckhu.domain.book.entity;

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
@Table(name = "popular_book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PopularBook extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "book_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_popularbook_book"))
  private Book book;

  @Enumerated(EnumType.STRING) // enum을 문자열로 처리
  @Column(nullable = false)
  private PeriodType period;

  @Column(name = "review_count")
  private Integer reviewCount;

  @Column private Double rating;

  @Column private Double score;

  @Column private Integer rank;
}
