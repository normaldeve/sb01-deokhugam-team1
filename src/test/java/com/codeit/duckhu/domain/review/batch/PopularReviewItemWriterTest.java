//package com.codeit.duckhu.domain.review.batch;
//
//import static org.mockito.Mockito.verify;
//
//import com.codeit.duckhu.domain.review.entity.PopularReview;
//import com.codeit.duckhu.domain.review.repository.PopularReviewRepository;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.batch.item.Chunk;
//
//class PopularReviewItemWriterTest {
//  @Mock private PopularReviewRepository popularReviewRepository;
//
//  @InjectMocks private PopularReviewItemWriter writer;
//
//  @BeforeEach
//  void setUp() {
//    MockitoAnnotations.openMocks(this);
//  }
//
//  @Test
//  @DisplayName("인기 리뷰 저장 성공")
//  void write_shouldSavePopularReviews() throws Exception {
//    // given
//    PopularReview popularReview = PopularReview.builder().build();
//    List<PopularReview> itemList = List.of(popularReview);
//    Chunk<PopularReview> items = new Chunk<>(itemList);
//
//    // when
//    writer.write(items);
//
//    // then
//    verify(popularReviewRepository).saveAll(itemList);
//  }
//}
