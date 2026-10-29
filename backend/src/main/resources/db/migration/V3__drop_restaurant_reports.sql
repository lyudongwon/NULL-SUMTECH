-- OPT-2를 음식점 목록 방식으로 되돌린 결정(decisions.md 2026-10-30, docs/erd.md > 음식점 추천)에 맞춰
-- V2가 더한 지도·사용자 보고 표와 칸을 지운다. V2는 이미 올라가 고치지 않고 이 파일로 되돌린다.
-- 목록 추천은 V1의 restaurants(id·name·category·address)와 restaurant_restriction_items만 쓴다.

DROP TABLE restaurant_report_items;
DROP TABLE restaurant_reports;

ALTER TABLE restaurants
    DROP COLUMN kakao_place_id,
    DROP COLUMN name_en,
    DROP COLUMN lat,
    DROP COLUMN lng,
    DROP COLUMN source;
