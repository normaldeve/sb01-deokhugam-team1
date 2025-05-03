DELETE FROM review_likes;
DELETE FROM comments;
DELETE FROM reviews;
DELETE FROM power_user;
DELETE FROM users;

INSERT INTO users (id, email, nickname, password, is_deleted, created_at)
VALUES
    ('123e4567-e89b-12d3-a456-426614174000', 'test@example.com', '테스트유저', 'test1234!', false, CURRENT_TIMESTAMP);

--책  같은 isbn 있으면 update, 없으면 insert
MERGE INTO books (
    id, title, author, description, publisher, published_date, isbn,
    thumbnail_url, review_count, rating, is_deleted, created_at, updated_at
) VALUES (
             '22222222-2222-2222-2222-222222222222',
             '예제 제목',
             '홍길동',
             '설명입니다',
             '출판사명',
             '2024-04-01',
             '1234567890123',
             'https://example.com/thumb.jpg',
             0,
             0.0,
             false,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP
         );

-- 리뷰 한 건
INSERT INTO reviews (id, content, rating, like_count, comment_count, is_deleted, user_id, book_id, created_at,updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', '좋아요', 5, 2, 4, false,
        '123e4567-e89b-12d3-a456-426614174000', '22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

-- 댓글 두 건
INSERT INTO comments (id, content, is_deleted, user_id, review_id, created_at, updated_at)
VALUES
    ('c1c1c1c1-aaaa-aaaa-aaaa-c1c1c1c1c1c1', '댓글1', false, '123e4567-e89b-12d3-a456-426614174000', '11111111-1111-1111-1111-111111111111', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('c2c2c2c2-bbbb-bbbb-bbbb-c2c2c2c2c2c2', '댓글2', false, '123e4567-e89b-12d3-a456-426614174000', '11111111-1111-1111-1111-111111111111', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 좋아요 한 건
INSERT INTO review_likes (id, user_id, review_id, created_at)
VALUES ('d1d1d1d1-eeee-eeee-eeee-d1d1d1d1d1d1', '123e4567-e89b-12d3-a456-426614174000', '11111111-1111-1111-1111-111111111111', CURRENT_TIMESTAMP);

-- 파워유저
INSERT INTO power_user (
  id, user_id, review_score_sum, like_count, comment_count, score, rank, period, created_at
) VALUES (
  '44444444-4444-4444-4444-444444444444',
  '123e4567-e89b-12d3-a456-426614174000',
  5.5, 2, 2, 10.5, 1, 'MONTHLY',
  CURRENT_TIMESTAMP
);