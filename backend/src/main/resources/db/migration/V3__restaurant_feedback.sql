-- OPT-5 음식점 성분 제보. docs/erd.md를 따른다.

-- "이 가게에 이 항목을 쓰는 음식이 있다"는 사용자 제보. 같은 사람이 같은 (가게, 항목)은 한 번만 센다.
CREATE TABLE restaurant_feedback (
    id            bigserial PRIMARY KEY,
    restaurant_id integer NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    user_id       bigint NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    item_code     text NOT NULL REFERENCES restriction_items (code),
    menu_name     text,
    note          text,
    created_at    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, user_id, item_code)
);
CREATE INDEX restaurant_feedback_restaurant_idx ON restaurant_feedback (restaurant_id, item_code);
