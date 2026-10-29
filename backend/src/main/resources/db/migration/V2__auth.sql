-- OPT-4 회원가입·로그인. docs/erd.md를 따른다.

-- 가입한 계정만 email·password_hash가 있다. 가입하지 않은 사용자(user_id만 발급)는 둘 다 null이다.
ALTER TABLE users
    ADD COLUMN email         text UNIQUE,
    ADD COLUMN password_hash text,
    ADD CONSTRAINT users_email_lowercase CHECK (email IS NULL OR email = lower(email)),
    ADD CONSTRAINT users_email_password CHECK ((email IS NULL) = (password_hash IS NULL));

-- 로그인 세션. 쿠키 값은 DB에 두지 않고 SHA-256 해시만 둔다.
CREATE TABLE auth_sessions (
    token_hash text PRIMARY KEY,
    user_id    bigint NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL
);
CREATE INDEX auth_sessions_user_id_idx ON auth_sessions (user_id);
