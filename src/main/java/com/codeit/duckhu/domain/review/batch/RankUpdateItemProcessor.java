//package com.codeit.duckhu.domain.review.batch;
//
//import com.codeit.duckhu.domain.review.entity.PopularReview;
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.stereotype.Component;
//
//@RequiredArgsConstructor
//@StepScope
//public class RankUpdateItemProcessor implements ItemProcessor<PopularReview, PopularReview> {
//
//  private int currentRank = 1;
//
//  @Override
//  public PopularReview process(PopularReview item) {
//    item.setRank(currentRank++);
//    return item;
//  }
//}
