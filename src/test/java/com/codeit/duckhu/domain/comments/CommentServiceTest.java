package com.codeit.duckhu.domain.comments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.duckhu.domain.comment.domain.Comment;
import com.codeit.duckhu.domain.comment.dto.CommentDto;
import com.codeit.duckhu.domain.comment.dto.CursorPageResponseCommentDto;
import com.codeit.duckhu.domain.comment.dto.request.CommentCreateRequest;
import com.codeit.duckhu.domain.comment.dto.request.CommentUpdateRequest;
import com.codeit.duckhu.domain.comment.exception.NoAuthorityException;
import com.codeit.duckhu.domain.comment.exception.NoCommentException;
import com.codeit.duckhu.domain.comment.repository.CommentRepository;
import com.codeit.duckhu.domain.comment.service.CommentMapper;
import com.codeit.duckhu.domain.comment.service.CommentService;
import com.codeit.duckhu.domain.notification.service.impl.NotificationServiceImpl;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.domain.review.service.impl.ReviewServiceImpl;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.service.UserServiceImpl;
import com.codeit.duckhu.global.type.Direction;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(profiles = "test")
@Import({TestJpaConfig.class})
class CommentServiceTest {

  @InjectMocks private CommentService commentService;

  @Mock private CommentRepository commentRepository;

  @Mock private UserServiceImpl userService;

  @Mock private ReviewServiceImpl reviewService;

  @Mock private NotificationServiceImpl notificationService;

  @Mock private CommentMapper commentMapper;

  @Mock private User mockUser;

  @Mock private Review review;

  @Test
  void create() {
    CommentCreateRequest request = new CommentCreateRequest();
    request.setUserId(UUID.randomUUID());
    request.setReviewId(UUID.randomUUID());
    request.setContent("test comment");

    CommentDto dto = new CommentDto();
    dto.setContent("test comment");

    given(userService.findByIdEntityReturn(any(UUID.class))).willReturn(mockUser);
    given(reviewService.findByIdEntityReturn(any(UUID.class))).willReturn(review);
    given(commentMapper.toDto(any(Comment.class))).willReturn(dto);

    CommentDto commentDto = commentService.create(request);

    assertEquals("test comment", commentDto.getContent());
  }

  @Test
  void update_success() {
    // given
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    CommentUpdateRequest updateRequest = new CommentUpdateRequest();
    updateRequest.setContent("updated content");

    Comment mockComment = mock(Comment.class);

    when(mockComment.getUser()).thenReturn(mockUser);
    when(mockUser.getId()).thenReturn(userId);

    CommentDto updatedDto = new CommentDto();
    updatedDto.setId(commentId);
    updatedDto.setContent("updated content");

    given(commentRepository.findById(any(UUID.class))).willReturn(Optional.of(mockComment));
    given(commentMapper.toDto(mockComment)).willReturn(updatedDto);

    // when
    CommentDto result = commentService.update(commentId, updateRequest, userId);

    // then
    assertEquals("updated content", result.getContent());
    assertEquals(commentId, result.getId());
  }

  @Test
  void update_fail_no_comment() {
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    CommentUpdateRequest updateRequest = new CommentUpdateRequest();
    updateRequest.setContent("updated content");

    given(commentRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    // then
    assertThrows(
        NoCommentException.class,
        () -> {
          commentService.update(commentId, updateRequest, userId);
        });
  }

  @Test
  void update_fail_no_authorities() {
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    CommentUpdateRequest updateRequest = new CommentUpdateRequest();
    updateRequest.setContent("updated content");

    Comment mockComment = mock(Comment.class);

    when(mockComment.getUser()).thenReturn(mockUser);
    when(mockUser.getId()).thenReturn(UUID.randomUUID());

    given(commentRepository.findById(any(UUID.class))).willReturn(Optional.of(mockComment));

    // then
    assertThrows(
        NoAuthorityException.class,
        () -> {
          commentService.update(commentId, updateRequest, userId);
        });
  }

  @Test
  void delete() {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Comment mockComment = mock(Comment.class);

    when(mockComment.getUser()).thenReturn(mockUser);
    when(mockUser.getId()).thenReturn(userId);
    given(commentRepository.findById(any(UUID.class))).willReturn(Optional.of(mockComment));
    given(mockComment.getReview()).willReturn(review);

    commentService.delete(commentId, userId);

    verify(commentRepository).deleteById(any(UUID.class));
  }

  @Test
  void delete_fail_no_comment() {
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    given(commentRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    // then
    assertThrows(
        NoCommentException.class,
        () -> {
          commentService.delete(commentId, userId);
        });
  }

  @Test
  void delete_fail_no_authorities() {
    UUID userId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();

    Comment mockComment = mock(Comment.class);

    when(mockComment.getUser()).thenReturn(mockUser);
    when(mockUser.getId()).thenReturn(UUID.randomUUID());

    given(commentRepository.findById(any(UUID.class))).willReturn(Optional.of(mockComment));

    // then
    assertThrows(
        NoAuthorityException.class,
        () -> {
          commentService.delete(commentId, userId);
        });
  }

  @Test
  void get() {
    UUID commentId = UUID.randomUUID();

    Comment comment = new Comment();
    comment.setContent("test comment");

    CommentDto dto = new CommentDto();
    dto.setContent("test comment");

    given(commentRepository.findById(any(UUID.class))).willReturn(Optional.of(comment));
    given(commentMapper.toDto(comment)).willReturn(dto);

    CommentDto result = commentService.get(commentId);

    assertEquals("test comment", result.getContent());
    assertNotNull(result);
  }

  @Test
  void getList() {
    UUID reviewId = UUID.randomUUID();

    Comment comment = mock(Comment.class);

    CommentDto dto = new CommentDto();
    dto.setId(UUID.randomUUID());
    dto.setCreatedAt(Instant.now());
    dto.setContent("test comment");

    List<Comment> comments = List.of(comment);
    Slice<Comment> slice = new SliceImpl<>(comments, PageRequest.of(0, 10), false);

    given(commentRepository.searchAll(eq(reviewId), eq("ASC"), any(), any(), eq(10)))
        .willReturn(slice);
    given(commentMapper.toDto(comment)).willReturn(dto);

    CursorPageResponseCommentDto responseCommentDto =
        commentService.getList(reviewId, Direction.ASC, null, Instant.now(), 10);

    assertThat(responseCommentDto.getContent()).hasSize(1);
    assertThat(responseCommentDto.getContent().get(0).getContent()).isEqualTo("test comment");
  }
}
