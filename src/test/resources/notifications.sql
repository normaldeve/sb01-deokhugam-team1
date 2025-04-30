DELETE FROM notifications;
DELETE FROM reviews;
DELETE FROM books;
DELETE FROM users;
-- 1) BOOKS – 리뷰가 참조하는 도서
INSERT INTO books (
    id, created_at, updated_at,
    title, author, publisher,
    published_date, isbn,
    is_deleted, rating, review_count, thumbnail_url, description
) VALUES (
             'cccccccc-cccc-cccc-cccc-cccccccccccc',
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP,
             '테스트 도서',
             '홍길동',
             '테스트출판사',
             '2020-01-01',
             'ISBN-1234',
             FALSE,        -- NOT NULL
             0.0,          -- JPA DDL엔 기본값 없을 수도 있으니 안전하게 넣어주고
             0,            -- 마찬가지
             NULL,         -- nullable
             NULL          -- nullable
         );

-- 2) USERS – 알림 수신자
INSERT INTO users (
    id, nickname, password, email,
    created_at, is_deleted
) VALUES (
             'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             'receiver',
             'pass',
             'recv@example.com',
             CURRENT_TIMESTAMP,
             FALSE
         );

-- 3) REVIEWS – 알림 대상 리뷰
INSERT INTO reviews (
    id, book_id, user_id, content, rating,
    like_count, comment_count,
    is_deleted, created_at, updated_at
) VALUES (
             'dddddddd-dddd-dddd-dddd-dddddddddddd',
             'cccccccc-cccc-cccc-cccc-cccccccccccc',
             'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             '정말 재미있었어요!', 5,
             0, 0,
             FALSE,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP
         );

-- 4) NOTIFICATIONS – 단일 알림
INSERT INTO notifications (
    id, review_id, user_id, review_title, content,
    confirmed, created_at, updated_at
) VALUES (
             'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
             'dddddddd-dddd-dddd-dddd-dddddddddddd',
             'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             '정말 재미있었어요!',
             '[trigger]님이 나의 리뷰를 좋아합니다.',
             FALSE,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP
         );
