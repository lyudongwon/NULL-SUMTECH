-- OPT-2 주변 음식점 지도와 사용자 보고(docs/erd.md > 음식점 추천, decisions.md 2026-10-30).
-- restaurants에 카카오맵 기본 정보 칸을 더하고, "이 가게에서 걸린다"는 사용자 보고 표 두 개를 만든다.
-- restaurant_restriction_items는 erd.md가 없앤다고 정했지만, 옛 추천 화면·흐름 S8이 아직 쓰므로 그 둘을 바꾼 뒤 다음 V 파일에서 지운다.

ALTER TABLE restaurants
    ADD COLUMN kakao_place_id text UNIQUE,         -- 카카오맵 장소 id. 같은 가게를 여러 번 가져와도 한 행(upsert)
    ADD COLUMN name_en        text,                -- REQ-8 영어 이름. 카카오맵은 한국어 이름만 줘서 없으면 null
    ADD COLUMN lat            double precision,
    ADD COLUMN lng            double precision,
    ADD COLUMN source         text NOT NULL DEFAULT 'SEED'; -- KAKAO(카카오 호출로 쌓임), SEED(시연용 시드)

CREATE TABLE restaurant_reports (
    restaurant_id integer NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    user_id       bigint NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    updated_at    timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (restaurant_id, user_id)
);

CREATE TABLE restaurant_report_items (
    restaurant_id integer NOT NULL,
    user_id       bigint NOT NULL,
    item_code     text NOT NULL REFERENCES restriction_items (code),
    PRIMARY KEY (restaurant_id, user_id, item_code),
    FOREIGN KEY (restaurant_id, user_id) REFERENCES restaurant_reports (restaurant_id, user_id) ON DELETE CASCADE
);
