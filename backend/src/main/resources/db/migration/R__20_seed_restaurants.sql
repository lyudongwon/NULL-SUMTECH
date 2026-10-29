-- OPT-2 음식점 시드(역할 ①).
-- TODO(확인): 음식점 데이터 출처가 정해지지 않아(erd.md 열린 질문 7) 예시 데이터만 넣는다.
-- 예시라는 표시는 이름 대신 추천 화면의 안내 문구("Sample list …")로 한다. 화면이 영어(REQ-8)라 종류는 영어로 쓰고,
-- 이름은 메뉴 이름처럼 한국어 원문으로 둔다. 주소는 모르므로 비워 둔다(화면은 주소가 있을 때만 보여 준다).

INSERT INTO restaurants (id, name, category, address) VALUES
    (1, '할랄 케밥', 'Middle Eastern', NULL),
    (2, '비건 샐러드', 'Salad', NULL),
    (3, '채식 비빔밥', 'Korean', NULL),
    (4, '인도 커리', 'Indian', NULL),
    (5, '분식집', 'Korean snacks', NULL)
ON CONFLICT (id) DO UPDATE
    SET name = EXCLUDED.name, category = EXCLUDED.category, address = EXCLUDED.address;

SELECT setval(pg_get_serial_sequence('restaurants', 'id'), GREATEST((SELECT max(id) FROM restaurants), 1));

-- 음식점이 충족하는(쓰지 않는) 제한 항목
DELETE FROM restaurant_restriction_items WHERE restaurant_id IN (1, 2, 3, 4, 5);
INSERT INTO restaurant_restriction_items (restaurant_id, item_code) VALUES
    (1, 'pork'), (1, 'alcohol'),
    (2, 'meat'), (2, 'pork'), (2, 'beef'), (2, 'seafood'), (2, 'egg'), (2, 'dairy'),
    (3, 'meat'), (3, 'pork'), (3, 'beef'), (3, 'seafood'),
    (4, 'pork'), (4, 'beef'), (4, 'alcohol');
