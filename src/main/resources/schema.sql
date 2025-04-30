-- 도서
CREATE TABLE books
(
    id             UUID PRIMARY KEY NOT NULL,
    created_at     TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP        NOT NULL,
    author         VARCHAR(255)     NOT NULL,
    description    TEXT, -- TEXT 그대로 사용
    is_deleted     BOOLEAN          NOT NULL DEFAULT FALSE,
    isbn           VARCHAR(255) UNIQUE,
    published_date DATE             NOT NULL,
    publisher      VARCHAR(255)     NOT NULL,
    rating         DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    review_count   INTEGER          NOT NULL DEFAULT 0,
    thumbnail_url  VARCHAR(255),
    title          VARCHAR(255)     NOT NULL
);

-- 사용자
CREATE TABLE users
(
    id         UUID PRIMARY KEY NOT NULL,
    nickname   VARCHAR(255)     NOT NULL,
    password   VARCHAR(255)     NOT NULL,
    created_at TIMESTAMP        NOT NULL DEFAULT now(),
    email      VARCHAR(255)     NOT NULL UNIQUE,
    is_deleted BOOLEAN          NOT NULL DEFAULT FALSE
);

-- 리뷰
CREATE TABLE reviews
(
    id            UUID PRIMARY KEY,
    book_id       UUID         NOT NULL,
    user_id       UUID         NOT NULL,
    content       VARCHAR(255) NOT NULL,
    rating        INT          NOT NULL CHECK (rating BETWEEN 0 AND 5),
    like_count    INT          NOT NULL DEFAULT 0,
    comment_count INT          NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL,

    CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 댓글
CREATE TABLE comments
(
    id         UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at TIMESTAMP        NOT NULL,
    user_id    UUID             NOT NULL,
    review_id  UUID             NOT NULL,
    content    VARCHAR(255)     NOT NULL, -- 225 → 255 수정
    is_deleted BOOLEAN          NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE
);

-- 알림
CREATE TABLE notifications
(
    id           UUID PRIMARY KEY NOT NULL,
    review_id    UUID,
    user_id      UUID,
    review_title VARCHAR(255)     NOT NULL,
    content      VARCHAR(255)     NOT NULL,
    confirmed    BOOLEAN          NOT NULL,
    created_at   TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP        NOT NULL,

    CONSTRAINT fk_notification_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE SET NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

-- 인기 유저
CREATE TABLE power_user
(
    id               UUID PRIMARY KEY NOT NULL,
    review_score_sum DOUBLE PRECISION,
    like_count       INT,
    comment_count    INT,
    user_id          UUID             NOT NULL,
    period           VARCHAR(20)      NOT NULL CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    created_at       TIMESTAMP        NOT NULL DEFAULT now(),
    score            DOUBLE PRECISION,
    rank             INT,

    CONSTRAINT fk_poweruser_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 인기 도서
CREATE TABLE popular_book
(
    id           UUID PRIMARY KEY NOT NULL,
    book_id      UUID             NOT NULL,
    created_at   TIMESTAMP        NOT NULL DEFAULT now(),
    period       VARCHAR(20)      NOT NULL CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    review_count INT,
    rating       DOUBLE PRECISION,
    rank         INT,
    score        DOUBLE PRECISION,

    CONSTRAINT fk_popularbook_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

-- 인기 리뷰
CREATE TABLE popular_reviews
(
    id            UUID PRIMARY KEY,
    review_id     UUID             NOT NULL,
    review_rating DOUBLE PRECISION NOT NULL CHECK (review_rating BETWEEN 0 AND 5),
    period        VARCHAR(20)      NOT NULL CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    created_at    TIMESTAMP        NOT NULL DEFAULT now(),
    like_count    INT,
    comment_count INT,
    score         DOUBLE PRECISION,
    rank          INT,

    CONSTRAINT fk_popular_reviews FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE
);

-- 리뷰 좋아요
CREATE TABLE review_likes
(
    id         UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP        NOT NULL DEFAULT now(),
    user_id    UUID             NOT NULL,
    review_id  UUID             NOT NULL,

    CONSTRAINT fk_review_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT uk_review_likes UNIQUE (review_id, user_id)
);
