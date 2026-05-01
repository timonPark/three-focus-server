CREATE TABLE terms (
    id          BIGSERIAL    PRIMARY KEY,
    type        VARCHAR(30)  NOT NULL UNIQUE,
    title       VARCHAR(100) NOT NULL,
    is_required BOOLEAN      NOT NULL,
    version     VARCHAR(10)  NOT NULL DEFAULT '1.0',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO terms (type, title, is_required) VALUES
    ('SERVICE_TERMS', '서비스 이용약관 동의', TRUE),
    ('PRIVACY_POLICY', '개인정보 처리방침 동의', TRUE),
    ('MARKETING', '마케팅 정보 수신 동의 (이메일/앱 푸시)', FALSE);

CREATE TABLE user_terms (
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    terms_id  BIGINT    NOT NULL REFERENCES terms (id),
    agreed    BOOLEAN   NOT NULL,
    agreed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, terms_id)
);

ALTER TABLE users DROP COLUMN terms_agreed;
