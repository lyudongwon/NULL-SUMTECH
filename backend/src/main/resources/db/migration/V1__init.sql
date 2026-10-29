-- 첫 스키마. docs/erd.md를 따른다. 표 변경(V 파일)은 역할 ①만 만든다(docs/roles.md).

-- REQ-4 사용자와 식단 제한
CREATE TABLE users (
    id         bigserial PRIMARY KEY, -- user_id. 회원 정보 없이 서버가 발급한다
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE restriction_presets (
    code text PRIMARY KEY,
    name text NOT NULL
);

CREATE TABLE restriction_items (
    code text PRIMARY KEY,
    name text NOT NULL
);

CREATE TABLE preset_items (
    preset_code text NOT NULL REFERENCES restriction_presets (code) ON DELETE CASCADE,
    item_code   text NOT NULL REFERENCES restriction_items (code) ON DELETE CASCADE,
    PRIMARY KEY (preset_code, item_code)
);

CREATE TABLE user_restrictions (
    user_id     bigint PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    preset_code text REFERENCES restriction_presets (code),
    version     integer NOT NULL DEFAULT 1,
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE user_restriction_items (
    user_id   bigint NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    item_code text NOT NULL REFERENCES restriction_items (code),
    enabled   boolean NOT NULL,
    PRIMARY KEY (user_id, item_code)
);

-- REQ-1·OPT-3 사진 메타데이터 (본문 저장 위치는 erd.md 열린 질문 1)
CREATE TABLE images (
    id          text PRIMARY KEY,
    width       integer,
    height      integer,
    source      text,
    mime_type   text,
    uploaded_at timestamptz NOT NULL DEFAULT now()
);

-- REQ-2·3·5·6, OPT-1 음식 DB와 숨은 성분
CREATE TABLE foods (
    id     serial PRIMARY KEY,
    name   text NOT NULL UNIQUE,
    source text NOT NULL DEFAULT 'SEED'
);

CREATE TABLE ingredients (
    id   serial PRIMARY KEY,
    name text NOT NULL UNIQUE
);

CREATE TABLE food_ingredients (
    food_id       integer NOT NULL REFERENCES foods (id) ON DELETE CASCADE,
    ingredient_id integer NOT NULL REFERENCES ingredients (id) ON DELETE CASCADE,
    evidence      text,
    confidence    text,
    basis         text,
    PRIMARY KEY (food_id, ingredient_id)
);

CREATE TABLE ingredient_restriction_items (
    ingredient_id integer NOT NULL REFERENCES ingredients (id) ON DELETE CASCADE,
    item_code     text NOT NULL REFERENCES restriction_items (code),
    PRIMARY KEY (ingredient_id, item_code)
);

-- OPT-2 음식점 추천
CREATE TABLE restaurants (
    id       serial PRIMARY KEY,
    name     text NOT NULL,
    category text,
    address  text
);

CREATE TABLE restaurant_restriction_items (
    restaurant_id integer NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    item_code     text NOT NULL REFERENCES restriction_items (code),
    PRIMARY KEY (restaurant_id, item_code)
);
