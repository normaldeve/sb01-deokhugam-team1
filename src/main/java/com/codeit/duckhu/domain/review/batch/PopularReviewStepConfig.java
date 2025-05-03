//package com.codeit.duckhu.domain.review.batch;
//
//import com.codeit.duckhu.domain.review.entity.PopularReview;
//import com.codeit.duckhu.domain.review.entity.Review;
//import com.codeit.duckhu.domain.review.repository.PopularReviewRepository;
//import com.codeit.duckhu.global.type.PeriodType;
//import jakarta.persistence.EntityManagerFactory;
//import java.util.Map;
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.database.JpaPagingItemReader;
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@Configuration
//@RequiredArgsConstructor
//public class PopularReviewStepConfig {
//
//  private final JobRepository jobRepository;
//  private final PlatformTransactionManager transactionManager;
//  private final EntityManagerFactory entityManagerFactory;
//  private final PopularReviewItemProcessor processor;
//  private final PopularReviewItemWriter writer;
//  private final PopularReviewRepository popularReviewRepository;
//
//  @Bean
//  public Step popularReviewStep() {
//    return new StepBuilder("popularReviewStep", jobRepository)
//        .<Review, PopularReview>chunk(100, transactionManager)
//        .reader(popularReviewItemReader())
//        .processor(processor)
//        .writer(writer)
//        .build();
//  }
//
//  @Bean
//  public JpaPagingItemReader<Review> popularReviewItemReader() {
//    JpaPagingItemReader<Review> reader = new JpaPagingItemReader<>();
//    reader.setEntityManagerFactory(entityManagerFactory);
//    reader.setQueryString("SELECT r FROM Review r WHERE r.isDeleted = false");
//    reader.setPageSize(100);
//    return reader;
//  }
//
//  @Bean
//  public Step rankUpdateStep(
//      JpaPagingItemReader<PopularReview> rankUpdateItemReader
//  ) {
//    return new StepBuilder("rankUpdateStep", jobRepository)
//        .<PopularReview, PopularReview>chunk(100, transactionManager)
//        .reader(rankUpdateItemReader)
//        .processor(rankUpdateItemProcessor())
//        .writer(rankUpdateItemWriter())
//        .build();
//  }
//
//  @Bean
//  @StepScope
//  public JpaPagingItemReader<PopularReview> rankUpdateItemReader(
//      @Value("#{jobParameters['period']}") String periodParam
//  ) {
//    RankUpdateItemReader reader = new RankUpdateItemReader(entityManagerFactory);
//    reader.setPeriodParam(periodParam);
//    reader.setParameterValues(Map.of("period", PeriodType.valueOf(periodParam)));
//    reader.setPageSize(100);
//    return reader;
//  }
//
//  @Bean
//  public RankUpdateItemProcessor rankUpdateItemProcessor() {
//    return new RankUpdateItemProcessor();
//  }
//
//  @Bean
//  public RankUpdateItemWriter rankUpdateItemWriter() {
//    return new RankUpdateItemWriter(popularReviewRepository);
//  }
//
//  @Bean(name = "rankUpdateItemReaderWithScope")
//  @StepScope
//  @ConditionalOnProperty(name = "spring.batch.job.enabled", havingValue = "true")
//  public RankUpdateItemReader rankUpdateItemReaderWithScope(
//      EntityManagerFactory entityManagerFactory,
//      @Value("#{jobParameters['period']}") String periodParam
//  ) {
//    RankUpdateItemReader reader = new RankUpdateItemReader(entityManagerFactory);
//    reader.setPeriodParam(periodParam);
//    reader.setParameterValues(Map.of("period", PeriodType.valueOf(periodParam)));
//    return reader;
//  }
//}
