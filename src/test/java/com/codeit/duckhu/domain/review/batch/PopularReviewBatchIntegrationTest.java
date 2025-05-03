//package com.codeit.duckhu.domain.review.batch;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.codeit.duckhu.domain.book.entity.Book;
//import com.codeit.duckhu.domain.book.repository.BookRepository;
//import com.codeit.duckhu.domain.review.entity.PopularReview;
//import com.codeit.duckhu.domain.review.entity.Review;
//import com.codeit.duckhu.domain.review.repository.PopularReviewRepository;
//import com.codeit.duckhu.domain.review.repository.ReviewRepository;
//import com.codeit.duckhu.domain.user.entity.User;
//import com.codeit.duckhu.domain.user.repository.UserRepository;
//import java.time.LocalDate;
//import java.util.List;
//import javax.sql.DataSource;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.batch.core.BatchStatus;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobExecution;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
//import org.springframework.test.context.TestPropertySource;
//
//@SpringBootTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
//@TestPropertySource(properties = {"spring.batch.jdbc.initialize-schema=always"})
//class PopularReviewBatchIntegrationTest {
//
//  @Autowired private JobLauncher jobLauncher;
//
//  @Autowired private Job popularReviewJob;
//
//  @Autowired private ReviewRepository reviewRepository;
//
//  @Autowired private PopularReviewRepository popularReviewRepository;
//
//  @Autowired private UserRepository userRepository;
//
//  @Autowired private BookRepository bookRepository;
//
//  @Autowired private DataSource dataSource;
//
//  private static int counter = 0;
//
//  @BeforeEach
//  void setUp() throws Exception {
//    ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
//    databasePopulator.addScript(
//        new ClassPathResource("org/springframework/batch/core/schema-h2.sql"));
//    databasePopulator.execute(dataSource);
//  }
//
//  @Test
//  @DisplayName("인기 리뷰 배치 통합 테스트 성공")
//  void popularReviewBatchJob_success() throws Exception {
//    // Given
//    User user =
//        User.builder()
//            .nickname("테스트유저")
//            .password("test1234")
//            .email("test" + (++counter) + "@example.com")
//            .build();
//    userRepository.save(user);
//
//    Book book =
//        Book.builder()
//            .title("테스트 책")
//            .author("테스트 작가")
//            .publisher("테스트 출판사")
//            .publishedDate(LocalDate.now())
//            .build();
//    bookRepository.save(book);
//
//    Review review =
//        Review.builder()
//            .content("훌륭한 책")
//            .rating(5)
//            .likeCount(10)
//            .commentCount(5)
//            .user(user)
//            .book(book)
//            .build();
//    reviewRepository.save(review);
//
//    // When
//    JobParameters jobParameters =
//        new JobParametersBuilder()
//            .addString("period", "DAILY")
//            .addLong("time", System.currentTimeMillis())
//            .toJobParameters();
//
//    JobExecution jobExecution = jobLauncher.run(popularReviewJob, jobParameters);
//
//    // Then
//    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
//
//    List<PopularReview> results = popularReviewRepository.findAll();
//    assertThat(results).isNotEmpty();
//    assertThat(results.get(0).getReviewRating()).isEqualTo(5.0);
//    assertThat(results.get(0).getScore()).isGreaterThan(0.0);
//  }
//}
