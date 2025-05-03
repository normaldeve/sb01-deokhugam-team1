package com.codeit.duckhu.domain.book.service;

import com.codeit.duckhu.domain.book.dto.PopularBookScore;
import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.entity.PopularBook;
import com.codeit.duckhu.domain.book.repository.BookRepository;
import com.codeit.duckhu.domain.book.repository.popular.PopularBookRepository;
import com.codeit.duckhu.domain.review.repository.ReviewRepository;
import com.codeit.duckhu.global.type.PeriodType;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PopularBookBatchService {

  private final BookRepository bookRepository;
  private final ReviewRepository reviewRepository;
  private final PopularBookRepository popularBookRepository;
  private final MeterRegistry meterRegistry;

  /**
   * 인기 도서 랭킹을 지정하는 배치 메서드 지정된 기간(일간, 주간, 월간, 역대)에 해당하는 리뷰 수와 평점을 기반으로 점수를 계산합니다.
   *
   * @param period 대상 기간 타입 (DAILY, WEEKLY, MONTHLY, ALL_TIME)
   */
  @Transactional
  public void savePopularBook(PeriodType period) {
    try {
      // 현재 시간과 주어진 기간의 시작 지점을 구합니다.
      Instant now = Instant.now();
      Instant from = period.toStartInstant(now);

      log.info("[Batch 시작] period={} | from={} ~ to={}", period, from, now);

      // 전체 도서를 가져오지 않고 리뷰가 1개 이상인 도서 가져오기
      List<Book> books = bookRepository.findBooksWithReviews();

      // 각 도서에 대해 해당 기간의 리뷰수, 평균 평점, 점수를 계산합니다.
      List<PopularBookScore> popularBookScores =
          books.stream()
              .map(
                  book -> {
                    int reviewCount =
                        reviewRepository.countByBookIdAndCreatedAtBetween(book.getId(), from, now);
                    double rating =
                        reviewRepository.calculateAverageRatingByBookIdAndCreatedAtBetween(
                            book.getId(), from, now);
                    // 점수는 리뷰수 * 0.4 + 평점 * 0.6로 계산합니다
                    double score = (reviewCount * 0.4) + (rating * 0.6);

                    log.info(
                        "도서: {} | 리뷰 수: {} | 평점: {} | 점수: {}",
                        book.getTitle(),
                        reviewCount,
                        rating,
                        score);

                    return new PopularBookScore(book, reviewCount, rating, score);
                  })
              .filter(sb -> sb.reviewCount() > 0)
              .sorted(Comparator.comparingDouble(PopularBookScore::score).reversed())
              .toList();

      // 이전에 저장된 해당 기간의 랭킹 정보를 삭제합니다.
      popularBookRepository.deleteByPeriod(period);
      log.info("[삭제 완료] 기존 PopularBook 삭제 - period={}", period);

      // 새로 계산된 점수를 기반으로 PopularBook 엔티티를 생성하고 랭킹을 부여합니다.
      List<PopularBook> popularBooks = new ArrayList<>();
      for (int i = 0; i < popularBookScores.size(); i++) {
        PopularBookScore pb = popularBookScores.get(i);

        log.info(
            "[랭킹 {}] 도서: {} | 리뷰 수: {} | 평점: {} | 점수: {}",
            i + 1,
            pb.book().getTitle(),
            pb.reviewCount(),
            pb.rating(),
            pb.score());

        PopularBook popularBook =
            PopularBook.builder()
                .book(pb.book())
                .period(period)
                .reviewCount(pb.reviewCount())
                .rating(pb.rating())
                .score(pb.score())
                .rank(i + 1)
                .build();

        popularBooks.add(popularBook);
      }

      popularBookRepository.saveAll(popularBooks);
      log.info("[저장 완료] PopularBook {}건 저장 완료", popularBooks.size());

      meterRegistry.counter("batch.book.popularBook.success", "period", period.name()).increment();
    } catch (Exception e) {
      log.info("[Batch 오류] period = {} 처리 중 오류 발생 : {}", period, e.getMessage());

      meterRegistry.counter("batch.book.popularBook.failure", "period", period.name()).increment();
    }
  }
}