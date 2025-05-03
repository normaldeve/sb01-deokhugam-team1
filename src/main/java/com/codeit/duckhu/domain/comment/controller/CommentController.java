package com.codeit.duckhu.domain.comment.controller;

import com.codeit.duckhu.domain.comment.dto.CommentDto;
import com.codeit.duckhu.domain.comment.dto.CursorPageResponseCommentDto;
import com.codeit.duckhu.domain.comment.dto.request.CommentCreateRequest;
import com.codeit.duckhu.domain.comment.dto.request.CommentUpdateRequest;
import com.codeit.duckhu.domain.comment.service.CommentService;
import com.codeit.duckhu.global.type.Direction;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @RequestBody CommentCreateRequest commentCreateRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(commentService.create(commentCreateRequest));
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> updateComment(
      @RequestBody CommentUpdateRequest commentUpdateRequest,
      @PathVariable UUID commentId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(commentService.update(commentId, commentUpdateRequest, userId));
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> deleteComment(
      @PathVariable UUID commentId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId) {
    commentService.delete(commentId, userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteCommentSoft(
      @PathVariable UUID commentId,
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId) {
    commentService.deleteSoft(commentId, userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseCommentDto> getCommentsList(
      @RequestParam UUID reviewId,
      @RequestParam(defaultValue = "DESC") Direction direction,
      @RequestParam(required = false) UUID cursorId,
      @RequestParam(required = false) Instant createdAt,
      @RequestParam(defaultValue = "30") int limit) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(commentService.getList(reviewId, direction, cursorId, createdAt, limit));
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<CommentDto> getComment(@PathVariable UUID commentId) {
    return ResponseEntity.status(HttpStatus.OK).body(commentService.get(commentId));
  }
}
