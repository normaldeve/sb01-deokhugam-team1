package com.codeit.duckhu.domain.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import com.codeit.duckhu.domain.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationDto;
import com.codeit.duckhu.domain.notification.entity.Notification;
import com.codeit.duckhu.domain.notification.exception.NotificationAccessDeniedException;
import com.codeit.duckhu.domain.notification.exception.NotificationNotFoundException;
import com.codeit.duckhu.domain.notification.mapper.NotificationMapper;
import com.codeit.duckhu.domain.notification.repository.NotificationRepository;
import com.codeit.duckhu.domain.notification.service.impl.NotificationServiceImpl;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.repository.ReviewRepository;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.global.type.PeriodType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private NotificationMapper notificationMapper;

  @Mock private ReviewRepository reviewRepository;

  @Mock private UserRepository userRepository;

  @Mock
  private MeterRegistry meterRegistry;

  @InjectMocks private NotificationServiceImpl notificationService;

  private UUID reviewId;
  private UUID triggerUserId;
  private UUID receiverId;

  @BeforeEach
  void setUp() {
    // 테스트 케이스별 UUID는 매번 랜덤으로 설정
    reviewId = UUID.randomUUID(); // 댓글이 달린 리뷰 ID
    triggerUserId = UUID.randomUUID(); // 댓글 or 좋아요를 누른 사용자
    receiverId = UUID.randomUUID(); // 리뷰 작성자 (알림 수신자)
  }

  @Nested
  @DisplayName("알림 생성")
  class CreateNotificationTest {

    @Test
    @DisplayName("리뷰 좋아요 알림 생성 성공")
    void createsNotificationForLike() {
      // given: 리뷰 내용과 좋아요 누른 사용자의 닉네임을 준비
      String nickname = "buzz";
      String reviewContent = "리뷰 내용";

      // 리뷰, 리뷰 작성자(수신자), 좋아요 누른 사용자(트리거 유저)를 mock으로 설정
      Review review = mock(Review.class);
      User receiver = mock(User.class);
      User triggerUser = mock(User.class);

      // 리뷰 객체가 리턴할 사용자(수신자)의 ID와 리뷰 내용을 정의
      // Mock 객체의 행동 설정
      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
      given(review.getUser()).willReturn(receiver);
      given(receiver.getId()).willReturn(receiverId);
      given(review.getContent()).willReturn(reviewContent);
      given(userRepository.findById(triggerUserId)).willReturn(Optional.of(triggerUser));
      given(triggerUser.getNickname()).willReturn(nickname);

      // 서비스 내부에서 생성할 알림 객체
      Notification notification =
          Notification.forLike(reviewId, receiverId, nickname, reviewContent);

      // 알림 저장 후 매퍼를 통해 변환된 DTO 예상 결과
      NotificationDto expectedDto =
          new NotificationDto(
              UUID.randomUUID(),
              receiverId,
              reviewId,
              reviewContent,
              notification.getContent(),
              false,
              Instant.now(),
              Instant.now());

      // Mock repository 동작 설정
      given(notificationRepository.save(any(Notification.class))).willReturn(notification);
      given(notificationMapper.toDto(notification)).willReturn(expectedDto);

      // when: 좋아요 알림 생성 서비스 호출
      NotificationDto result = notificationService.createNotifyByLike(reviewId, triggerUserId);

      // then: 반환값 검증 및 repository, mapper 호출 여부 검증
      assertThat(result.reviewId()).isEqualTo(reviewId);
      assertThat(result.userId()).isEqualTo(receiverId);
      assertThat(result.content()).isEqualTo(notification.getContent());

      then(reviewRepository).should().findById(reviewId);
      then(userRepository).should().findById(triggerUserId);
      then(notificationRepository).should().save(any(Notification.class));
      then(notificationMapper).should().toDto(notification);
    }

    @Test
    @DisplayName("리뷰 댓글 알림 생성 성공")
    void createsNotificationForComment() {
      // given: 댓글 작성자의 닉네임, 댓글 내용, 리뷰 내용을 정의
      String nickname = "buzz";
      String comment = "이 리뷰 좋네요!";
      String reviewContent = "맛있었어요.";

      // 리뷰, 수신자(리뷰 작성자), 댓글 작성자(mock)를 설정
      Review review = mock(Review.class);
      User receiver = mock(User.class);
      User triggerUser = mock(User.class);

      // 리뷰가 참조하는 작성자와 그 ID, 리뷰 내용을 정의
      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
      given(review.getUser()).willReturn(receiver);
      given(receiver.getId()).willReturn(receiverId);
      given(review.getContent()).willReturn(reviewContent);

      // 댓글 작성자(트리거 유저)의 닉네임 정의
      given(userRepository.findById(triggerUserId)).willReturn(Optional.of(triggerUser));
      given(triggerUser.getNickname()).willReturn(nickname);

      // 알림 객체 생성 (댓글용)
      Notification notification =
          Notification.forComment(reviewId, receiverId, nickname, comment, reviewContent);

      // 매핑 후 기대되는 DTO 정의
      NotificationDto expectedDto =
          new NotificationDto(
              UUID.randomUUID(),
              receiverId,
              reviewId,
              reviewContent,
              notification.getContent(),
              false,
              Instant.now(),
              Instant.now());

      // 두 번 save() 호출됨을 stub
      given(notificationRepository.save(any(Notification.class))).willReturn(notification);
      given(notificationMapper.toDto(notification)).willReturn(expectedDto);

      // when: 댓글 알림 생성 서비스 호출
      NotificationDto result =
          notificationService.createNotifyByComment(reviewId, triggerUserId, comment);

      // then: 결과 및 각 mock 객체의 호출 여부 검증
      // then: 저장은 두 번 호출
      then(reviewRepository).should().findById(reviewId);
      then(userRepository).should().findById(triggerUserId);
      then(notificationRepository).should(times(2)).save(any(Notification.class));
      then(notificationMapper).should(times(1)).toDto(notification);

      assertThat(result.reviewId()).isEqualTo(reviewId);
      assertThat(result.userId()).isEqualTo(receiverId);
      assertThat(result.content()).isEqualTo(notification.getContent());
    }

    @Test
    @DisplayName("인기 리뷰 알림 생성 성공")
    void createsNotificationForPopularReview() {
      // given: 주간 랭킹 5위 리뷰라고 가정
      PeriodType period = PeriodType.WEEKLY;
      int rank = 5;
      String reviewContent = "인기 리뷰 내용";
      Review review = mock(Review.class);

      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
      given(review.getContent()).willReturn(reviewContent);

      Notification notification = Notification.forPopularReview(reviewId, receiverId, period, rank, reviewContent);
      NotificationDto expectedDto = new NotificationDto(
          UUID.randomUUID(),
          receiverId,
          reviewId,
          reviewContent,
          notification.getContent(),
          false,
          Instant.now(),
          Instant.now());

      given(notificationRepository.save(any(Notification.class))).willReturn(notification);
      given(notificationMapper.toDto(notification)).willReturn(expectedDto);

      // when: 인기리뷰 알림 생성 호출
      NotificationDto result = notificationService.createNotifyByPopularReview(
          reviewId, receiverId, period, rank);

      // then
      assertThat(result.reviewId()).isEqualTo(reviewId);
      assertThat(result.userId()).isEqualTo(receiverId);
      assertThat(result.content()).contains("인기 리뷰"); // 메시지에 랭크 정보 포함 여부 확인

      then(reviewRepository).should().findById(reviewId);
      then(notificationRepository).should(times(1)).save(any(Notification.class));
      then(notificationMapper).should(times(1)).toDto(notification);
    }
  }

  @Nested
  @DisplayName("알림 목록 조회")
  class GetNotificationsTest {

    private UUID rid = UUID.randomUUID();

    @Test
    @DisplayName("DESC 정렬, 커서 없는 경우 페이지 한정 조회 및 hasNext=false")
    void getNotificationsDescNoCursor() {
      // given
      List<Notification> raw = IntStream.range(0, 3)
          .mapToObj(i -> Notification.forLike(reviewId, rid, "u" + i, "c" + i))
          .collect(Collectors.toList());
      String direction = "DESC";
      Instant cursor = null;
      int limit = 3;

      given(notificationRepository.findDescNoCursor(rid,
          PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "createdAt"))))
          .willReturn(raw);

      given(notificationRepository.countByReceiverId(rid)).willReturn(3L);
      List<NotificationDto> dtoList = raw.stream()
          .map(n -> new NotificationDto(n.getId(), rid, n.getReviewId(), n.getContent(),
              n.getContent(), false, n.getCreatedAt(), n.getUpdatedAt()))
          .collect(Collectors.toList());
      dtoList.forEach(dto -> given(notificationMapper.toDto(any())).willReturn(dto));

      // when
      CursorPageResponseNotificationDto page = notificationService.getNotifications(rid, direction,
          cursor, limit);

      // then
      assertThat(page.content()).hasSize(3);
      assertThat(page.hasNext()).isFalse();
      assertThat(page.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("ASC 정렬, 커서 있는 경우 hasNext=true")
    void getNotificationsAscWithCursor() {
      // given
      Instant base = Instant.now();
      Instant lastCursor = Instant.now().minusSeconds(10);
      String direction = "ASC";
      int limit = 2;
      List<Notification> raw = IntStream.range(0, 3)
          .mapToObj(i -> {
            Notification n = Notification.forLike(reviewId, rid, "u"+i, "c"+i);
            // ensure createdAt > cursor
            ReflectionTestUtils.setField(n, "createdAt", base.plusSeconds(i));
            return n;
          })
          .collect(Collectors.toList());

      Pageable pageable = PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.ASC, "createdAt"));
      given(notificationRepository.findAscWithCursor(rid, lastCursor, pageable)).willReturn(raw);
      given(notificationRepository.countByReceiverId(rid)).willReturn(5L);

      raw.subList(0, limit).forEach(n ->
          given(notificationMapper.toDto(n))
              .willReturn(new NotificationDto(
                  n.getId(), rid, n.getReviewId(),
                  n.getContent(), n.getContent(),
                  false, n.getCreatedAt(), n.getUpdatedAt()))
      );

      // when
      CursorPageResponseNotificationDto page =
          notificationService.getNotifications(rid, direction, lastCursor, limit);

      // then
      assertThat(page.content()).hasSize(limit);
      assertThat(page.hasNext()).isTrue();
      assertThat(page.totalElements()).isEqualTo(5);
      assertThat(page.nextCursor()).isNotNull();
    }
  }
  @Nested
  @DisplayName("알림 읽은 상태 업데이트")
  class UpdateNotificationTest {

    @Test
    @DisplayName("알림 ID에 해당하는 알림의 읽음 상태를 true로 업데이트한다")
    void updateNotificationConfirmedSuccessfully() {
      // given: 읽지 않은 알림 객체
      UUID notificationId = UUID.randomUUID();
      Notification notification =
          Notification.forLike(UUID.randomUUID(), receiverId, "buzz", "타이틀");

      NotificationDto expectedDto =
          new NotificationDto(
              notificationId,
              receiverId,
              notification.getReviewId(),
              "타이틀",
              notification.getContent(),
              true,
              Instant.now(),
              Instant.now());

      given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
      given(notificationMapper.toDto(notification)).willReturn(expectedDto);

      // when
      NotificationDto result =
          notificationService.updateConfirmedStatus(notificationId, receiverId, true);

      // then: 알림이 읽음 상태로 업데이트됐는지 확인
      assertThat(notification.isConfirmed()).isTrue();
      assertThat(result.confirmed()).isTrue();
      then(notificationRepository).should(times(1)).findById(notificationId);
      then(notificationRepository).should(times(1)).save(notification);
      then(notificationMapper).should(times(1)).toDto(notification);
    }

    @Test
    @DisplayName("수신자 불일치 시 접근 권한 오류")
    void throwsAccessDeniedIfReceiverMismatch() {
      // given
      UUID nid = UUID.randomUUID();
      Notification notification = Notification.forLike(UUID.randomUUID(), receiverId, "u", "t");
      UUID otherUser = UUID.randomUUID();

      given(notificationRepository.findById(nid)).willReturn(Optional.of(notification));

      // then
      assertThatThrownBy(() -> notificationService.updateConfirmedStatus(nid, otherUser, true))
          .isInstanceOf(NotificationAccessDeniedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 알림 ID로 조회 시 예외가 발생한다")
    void throwsNotificationNotFoundExceptionIfNotFound() {
      UUID notificationId = UUID.randomUUID();
      given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

      assertThatThrownBy(
              () -> notificationService.updateConfirmedStatus(notificationId, receiverId, true))
          .isInstanceOf(NotificationNotFoundException.class);

      then(notificationRepository).should(times(1)).findById(notificationId);
    }

    @Test
    @DisplayName("사용자의 모든 알림을 읽음 처리한다")
    void updateAllNotificationsConfirmedSuccessfully() {
      // when
      notificationService.updateAllConfirmedStatus(receiverId);

      // then: bulk update 메서드 호출 검증
      then(notificationRepository).should(times(1))
          .bulkMarkAsConfirmed(eq(receiverId), any(Instant.class));
    }
  }

  @Nested
  @DisplayName("알림 삭제 스케줄러")
  class DeleteNotificationTest {

    @Test
    @DisplayName("매일 00:30 기준 1분 지난 확인된 알림 삭제")
    void deleteConfirmedNotificationsOlderThanOneMinute() {
      // given
      Counter counter = mock(Counter.class);
      given(meterRegistry.counter(anyString())).willReturn(counter);

      // when
      notificationService.deleteConfirmedNotificationsOlderThanAWeek();

      // then
      then(notificationRepository).should(times(1))
          .deleteOldConfirmedNotifications(argThat(cutoff ->
              Duration.between(cutoff, Instant.now()).toMinutes() >= 1
          ));
    }
  }
}
