//package com.codeit.duckhu.domain.review.batch;
//
//import com.codeit.duckhu.domain.review.entity.PopularReview;
//import com.codeit.duckhu.domain.review.repository.PopularReviewRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.stereotype.Component;
//
//@RequiredArgsConstructor
//@StepScope
//public class RankUpdateItemWriter implements ItemWriter<PopularReview> {
//
//  private final PopularReviewRepository popularReviewRepository;
//
//  @Override
//  public void write(Chunk<? extends PopularReview> chunk) {
//    popularReviewRepository.saveAll(chunk.getItems());
//  }
//}
