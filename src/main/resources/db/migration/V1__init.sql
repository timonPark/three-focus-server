CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE todos (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    is_completed BOOLEAN     NOT NULL DEFAULT FALSE,
    date         DATE        NOT NULL,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_todos_user_date ON todos (user_id, date);

CREATE TABLE top3 (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    todo_id     BIGINT NOT NULL REFERENCES todos (id) ON DELETE CASCADE,
    date        DATE   NOT NULL,
    order_index INT    NOT NULL,
    UNIQUE (user_id, date, order_index)
);

CREATE TABLE schedules (
    id         BIGSERIAL PRIMARY KEY,
    todo_id    BIGINT NOT NULL UNIQUE REFERENCES todos (id) ON DELETE CASCADE,
    start_time TIME   NOT NULL
);

CREATE TABLE shares (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    date        DATE         NOT NULL,
    share_token VARCHAR(36)  NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
