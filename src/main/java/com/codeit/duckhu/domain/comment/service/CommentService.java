package com.codeit.duckhu.domain.comment.service;

import com.codeit.duckhu.domain.comment.domain.Comment;
import com.codeit.duckhu.domain.comment.dto.CommentDto;
import com.codeit.duckhu.domain.comment.dto.CursorPageResponseCommentDto;
import com.codeit.duckhu.domain.comment.dto.request.CommentCreateRequest;
import com.codeit.duckhu.domain.comment.dto.request.CommentUpdateRequest;
import com.codeit.duckhu.domain.comment.exception.NoAuthorityException;
import com.codeit.duckhu.domain.comment.exception.NoCommentException;
import com.codeit.duckhu.domain.comment.repository.CommentRepository;
import com.codeit.duckhu.domain.notification.exception.NotificationException;
import com.codeit.duckhu.domain.notification.service.impl.NotificationServiceImpl;
import com.codeit.duckhu.domain.review.service.impl.ReviewServiceImpl;
import com.codeit.duckhu.domain.user.service.UserServiceImpl;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.codeit.duckhu.global.type.Direction;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
  private final CommentRepository repository;
  private final CommentMapper commentMapper;

  private final UserServiceImpl userService;
  private final ReviewServiceImpl reviewService;
  private final NotificationServiceImpl notificationService;

  public CommentDto get(UUID id) {
    Comment comment =
        repository
            .findById(id)
            .orElseThrow(() -> new NoCommentException(ErrorCode.NOT_FOUND_COMMENT));

    return commentMapper.toDto(comment);
  }

  public CursorPageResponseCommentDto getList(
      UUID reviewId, Direction direction, UUID cursorId, Instant createdAt, int limit) {
    Slice<Comment> slice = repository.searchAll(reviewId, direction.toString(), createdAt, cursorId, limit);

    // 삭제되지 않은 댓글만 필터링
    List<CommentDto> list = slice.getContent().stream()
        .filter(comment -> !comment.getIsDeleted())  // 삭제되지 않은 댓글만 포함
        .map(commentMapper::toDto).toList();

    CursorPageResponseCommentDto response = new CursorPageResponseCommentDto();
    response.setContent(list);
    response.setSize(list.size());
    response.setHasNext(slice.hasNext());

    if (!list.isEmpty()) {
      CommentDto lastComment = list.get(list.size() - 1);
      response.setNextCursor(lastComment.getId().toString());
      response.setNextAfter(lastComment.getCreatedAt());
    }

    // 삭제되지 않은 댓글 수를 조회합니다 TODO: 성능 이슈 리팩토링
    response.setTotalElements((long) repository.findByReview_IdAndIsDeletedFalse(reviewId).size());

    return response;
  }

  public CommentDto create(CommentCreateRequest request) {
    Comment comment =
        Comment.builder()
            .user(userService.findByIdEntityReturn(request.getUserId()))
            .review(reviewService.findByIdEntityReturn(request.getReviewId()))
            .content(request.getContent())
            .isDeleted(false)
            .build();

    // 리뷰의 댓글 수 증가
    comment.getReview().increaseCommentCount();
    
    repository.save(comment);

    // 알림 생성 로직 이 과정에서 comment의 저장은 영향이 가지 않도록 try catch문으로 잡는다
    try {
      log.info("알림 생성 시작");
      notificationService.createNotifyByComment(
          request.getReviewId(), request.getUserId(), request.getContent());
      log.info("알림 생성 완료");
    } catch (NotificationException e) {
      // 예외 로깅만 하고 무시
      log.debug("알림 생성 실패: {}", e.getMessage(), e);
    }

    return commentMapper.toDto(comment);
  }

  public void delete(UUID id, UUID userId) {
    Comment comment =
        repository
            .findById(id)
            .orElseThrow(() -> new NoCommentException(ErrorCode.NOT_FOUND_COMMENT));

    if (comment.getUser().getId().equals(userId)) {
      // 리뷰의 댓글 수 감소
      comment.getReview().decreaseCommentCount();
      
      repository.deleteById(id);
    } else {
      throw new NoAuthorityException(ErrorCode.NO_AUTHORITY_USER);
    }
  }

  public void deleteSoft(UUID id, UUID userId) {
    Comment comment =
        repository
            .findById(id)
            .orElseThrow(() -> new NoCommentException(ErrorCode.NOT_FOUND_COMMENT));

    if (comment.getUser().getId().equals(userId)) {
      // 이미 삭제된 상태가 아닐 때만 리뷰의 댓글 수 감소
      if (!comment.getIsDeleted()) {
        comment.getReview().decreaseCommentCount();
      }
      
      comment.markAsDeleted(true);
    } else {
      throw new NoAuthorityException(ErrorCode.NO_AUTHORITY_USER);
    }
  }

  public CommentDto update(UUID id, CommentUpdateRequest request, UUID userId) {
    Comment comment =
        repository
            .findById(id)
            .orElseThrow(() -> new NoCommentException(ErrorCode.NOT_FOUND_COMMENT));

    if (comment.getUser().getId().equals(userId)) {
      comment.setContent(request.getContent());
    } else {
      throw new NoAuthorityException(ErrorCode.NO_AUTHORITY_USER);
    }

    return commentMapper.toDto(comment);
  }
}
