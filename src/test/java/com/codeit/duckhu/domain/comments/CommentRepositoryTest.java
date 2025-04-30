package com.codeit.duckhu.domain.comments;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.duckhu.domain.book.entity.Book;
import com.codeit.duckhu.domain.book.repository.BookRepository;
import com.codeit.duckhu.domain.comment.domain.Comment;
import com.codeit.duckhu.domain.comment.dto.CommentDto;
import com.codeit.duckhu.domain.comment.repository.CommentRepository;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.review.repository.ReviewRepository;
import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.global.type.Direction;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
class CommentRepositoryTest {

  @Autowired
  CommentRepository commentRepository;

  @Autowired
  ReviewRepository reviewRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  BookRepository bookRepository;

  private User savedUser;
  private Review savedReview;
  private Comment comment;

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
    reviewRepository.deleteAll();
    userRepository.deleteAll();
    bookRepository.deleteAll();

    savedUser = User.builder()
        .email("test@example.com")
        .password("password")
        .nickname("tester")
        .build();
    userRepository.save(savedUser);

    Book book = Book.builder().title("book")
        .author("author")
        .description("test")
        .isbn("009900")
        .publishedDate(LocalDate.now())
        .publisher("test")
        .build();
    bookRepository.save(book);

    savedReview = Review.builder()
        .user(savedUser)
        .content("리뷰 내용")
        .book(book)
        .rating(3)
        .build();
    reviewRepository.save(savedReview);

    comment = Comment.builder()
        .user(savedUser)
        .review(savedReview)
        .content("test comment")
        .build();
  }

  @Test
  void save() {
    Comment savedComment = commentRepository.save(comment);

    assertThat(savedComment.getId()).isNotNull();
    assertThat(savedComment.getCreatedAt()).isNotNull();
    assertThat(savedComment.getContent()).isEqualTo(comment.getContent());
    assertThat(savedComment.getUser().getId()).isEqualTo(savedUser.getId());
    assertThat(savedComment.getReview().getId()).isEqualTo(savedReview.getId());
  }

  @Test
  void findByReview_Id() {
    commentRepository.save(comment);

    List<Comment> list = commentRepository.findByReview_Id(savedReview.getId());
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getContent()).isEqualTo("test comment");
    assertThat(list.get(0).getReview().getId()).isEqualTo(savedReview.getId());
  }

  @Test
  void searchAll() {
    Comment saved = commentRepository.save(comment);

    Slice<Comment> slice = commentRepository.searchAll(
        savedReview.getId(),
        Direction.ASC.toString(),
        Instant.EPOCH,
        null,
        10
    );

    assertThat(slice.getContent()).contains(saved);
  }

}
