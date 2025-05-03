//package com.codeit.duckhu.domain.review.batch;
//
//import com.codeit.duckhu.domain.review.entity.PopularReview;
//import com.codeit.duckhu.domain.review.entity.Review;
//import com.codeit.duckhu.global.exception.DomainException;
//import com.codeit.duckhu.global.exception.ErrorCode;
//import com.codeit.duckhu.global.type.PeriodType;
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//@StepScope
//@RequiredArgsConstructor
//public class PopularReviewItemProcessor implements ItemProcessor<Review, PopularReview> {
//
//  @Value("#{jobParameters['period']}")
//  private String periodParam;
//
//  @Override
//  public PopularReview process(Review review) throws Exception {
//    if (periodParam == null || periodParam.isEmpty()) {
//      throw new DomainException(ErrorCode.BATCH_PARAMETER_ERROR);
//    }
//
//    PeriodType period = PeriodType.valueOf(periodParam);
//
//    double score = (review.getLikeCount() * 0.3) + (review.getCommentCount() * 0.7);
//
//    return PopularReview.builder()
//        .review(review)
//        .period(period) // 주입된 기간
//        .score(score)
//        .rank(0) // 초기화 (나중에 Step에서 랭킹 정렬 처리)
//        .likeCount(review.getLikeCount())
//        .commentCount(review.getCommentCount())
//        .reviewRating((double) review.getRating())
//        .build();
//  }
//}
