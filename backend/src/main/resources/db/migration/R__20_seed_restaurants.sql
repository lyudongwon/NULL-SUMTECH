-- OPT-2 음식점 시드(역할 ①).
-- TODO(확인): 음식점 데이터 출처가 정해지지 않아(erd.md 열린 질문 7) 이름에 "(예시)"를 붙인 가짜 데이터만 넣는다.

INSERT INTO restaurants (id, name, category, address) VALUES
    (1, '(예시) 할랄 케밥', '중동식', 'TODO(확인)'),
    (2, '(예시) 비건 샐러드', '샐러드', 'TODO(확인)'),
    (3, '(예시) 채식 비빔밥', '한식', 'TODO(확인)'),
    (4, '(예시) 인도 커리', '인도식', 'TODO(확인)'),
    (5, '(예시) 분식집', '분식', 'TODO(확인)')
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
