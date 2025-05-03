//package com.codeit.duckhu.domain.review.batch;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//import com.codeit.duckhu.domain.review.repository.PopularReviewRepository;
//import com.codeit.duckhu.global.exception.DomainException;
//import jakarta.persistence.EntityManagerFactory;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@ExtendWith(MockitoExtension.class)
//class PopularReviewStepConfigTest {
//
//  @Mock private JobRepository jobRepository;
//  @Mock private PlatformTransactionManager transactionManager;
//  @Mock private EntityManagerFactory entityManagerFactory;
//  @Mock private PopularReviewItemProcessor processor;
//  @Mock private PopularReviewItemWriter writer;
//  @Mock private PopularReviewRepository popularReviewRepository;
//
//  @Test
//  @DisplayName("인기 리뷰 점수 계산 Step 설정 테스트")
//  void testStepConfiguration() {
//    // Given
//    PopularReviewStepConfig stepConfig =
//        new PopularReviewStepConfig(
//            jobRepository,
//            transactionManager,
//            entityManagerFactory,
//            processor,
//            writer,
//            popularReviewRepository);
//
//    // Config 테스트 로직 구현
//    assertThat(stepConfig).isNotNull();
//  }
//
//  @Test
//  @DisplayName("인기 리뷰 점수 계산 공식 테스트")
//  void testScoreCalculation() {
//    // Given - 좋아요 10개, 댓글 5개인 리뷰
//    int likeCount = 10;
//    int commentCount = 5;
//
//    // When - 점수 계산식 적용
//    double likeWeight = 0.3;
//    double commentWeight = 0.7;
//    double score = (likeCount * likeWeight) + (commentCount * commentWeight);
//
//    // Then - 계산 결과 확인
//    assertThat(score).isEqualTo(6.5);
//  }
//
//  @Test
//  @DisplayName("잘못된 period 파라미터가 들어오면 예외가 발생")
//  void testInvalidPeriodThrowsException() {
//    // Given
//    RankUpdateItemReader reader = new RankUpdateItemReader(entityManagerFactory);
//
//    // 잘못된 값 세팅
//    reader.setPeriodParam("INVALID_PERIOD");
//
//    // When / Then
//    assertThatThrownBy(reader::init)
//        .isInstanceOf(DomainException.class)
//        .hasMessageContaining("잘못된 요청입니다.");
//  }
//}
