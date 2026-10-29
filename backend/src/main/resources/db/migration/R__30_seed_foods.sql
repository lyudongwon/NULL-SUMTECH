-- 음식·재료 시드(역할 ③). 내용이 바뀌면 Flyway가 다시 실행하므로 여러 번 실행돼도 결과가 같게 쓴다.
-- 음식은 docs/erd.md > 시드 데이터 최소 요구와 docs/scenarios.yaml의 예시 메뉴를 덮는다.
-- "된장찌개"는 넣지 않는다. manual-input-unknown-menu가 DB에 없는 경로(만개의 레시피 → AI 추정)를 확인한다.
-- evidence·confidence·basis 값은 docs/api.md > 열거형을 따른다.
--   CONFIRMED + DB        = 음식 DB에 등록된 재료("확인")
--   INFERRED  + COOKING_METHOD = 조리법에서 추정한 숨은 재료("추정"). 육수·젓갈·맛술 같은 것들이다
-- 음식별 재료 값은 ③이 정했다(docs/erd.md "음식별 재료 값은 ③이 정한다").
-- REQ-5가 런타임에 넣는 음식(source = RECIPE_SEARCH·AI_ESTIMATE)은 아래 id 범위를 쓰지 않는다.

-- 1. 재료 사전
INSERT INTO ingredients (id, name) VALUES
    (1, '소고기'),
    (2, '소뼈 육수'),
    (3, '대파'),
    (4, '소금'),
    (5, '콩나물'),
    (6, '멸치 육수'),
    (7, '다진 마늘'),
    (8, '국간장'),
    (9, '양상추'),
    (10, '방울토마토'),
    (11, '오이'),
    (12, '발사믹 드레싱'),
    (13, '양송이버섯'),
    (14, '쌀'),
    (15, '버터'),
    (16, '파마산 치즈'),
    (17, '치킨 스톡'),
    (18, '돼지고기'),
    (19, '김치'),
    (20, '두부'),
    (21, '김치 속 젓갈'),
    (22, '맛술'),
    (23, '쌀밥'),
    (24, '시금치'),
    (25, '당근'),
    (26, '계란'),
    (27, '고추장'),
    (28, '고추장 속 다진 돼지고기'),
    (29, '소면'),
    (30, '양념장'),
    (31, '양념장 속 땅콩'),
    (32, '햄'),
    (33, '소시지'),
    (34, '라면 사리'),
    (35, '사골 육수'),
    (36, '떡'),
    (37, '어묵'),
    (38, '고춧가루')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval(pg_get_serial_sequence('ingredients', 'id'), GREATEST((SELECT max(id) FROM ingredients), 1));

-- 2. 재료가 걸리는 제한 항목 전부(docs/api.md: 돼지고기는 pork와 meat, 소고기는 beef와 meat에 걸린다)
DELETE FROM ingredient_restriction_items WHERE ingredient_id BETWEEN 1 AND 38;
INSERT INTO ingredient_restriction_items (ingredient_id, item_code) VALUES
    (1, 'beef'), (1, 'meat'),            -- 소고기
    (2, 'beef'), (2, 'meat'),            -- 소뼈 육수
    (6, 'seafood'),                      -- 멸치 육수
    (15, 'dairy'),                       -- 버터
    (16, 'dairy'),                       -- 파마산 치즈
    (17, 'meat'),                        -- 치킨 스톡
    (18, 'pork'), (18, 'meat'),          -- 돼지고기
    (21, 'seafood'),                     -- 김치 속 젓갈
    (22, 'alcohol'),                     -- 맛술
    (26, 'egg'),                         -- 계란
    (28, 'pork'), (28, 'meat'),          -- 고추장 속 다진 돼지고기
    (31, 'peanut'),                      -- 양념장 속 땅콩
    (32, 'pork'), (32, 'meat'),          -- 햄
    (33, 'pork'), (33, 'meat'),          -- 소시지
    (35, 'beef'), (35, 'meat'),          -- 사골 육수
    (37, 'seafood');                     -- 어묵

-- 3. 음식
INSERT INTO foods (id, name, source) VALUES
    (1, '설렁탕', 'SEED'),
    (2, '콩나물국', 'SEED'),
    (3, '그린샐러드', 'SEED'),
    (4, '버섯리조또', 'SEED'),
    (5, '김치찌개', 'SEED'),
    (6, '비빔밥', 'SEED'),
    (7, '비빔국수', 'SEED'),
    (8, '부대찌개', 'SEED'),
    (9, '떡볶이', 'SEED')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, source = EXCLUDED.source;

SELECT setval(pg_get_serial_sequence('foods', 'id'), GREATEST((SELECT max(id) FROM foods), 1));

-- 4. 음식에 들어 있는 재료
DELETE FROM food_ingredients WHERE food_id BETWEEN 1 AND 9;
INSERT INTO food_ingredients (food_id, ingredient_id, evidence, confidence, basis) VALUES
    -- 설렁탕: 소고기가 CONFIRMED라 "고기"·"소고기"·채식에서 R1 "불가"가 나온다(scan-menu-board, diet-toggle-only)
    (1, 1, 'CONFIRMED', 'HIGH', 'DB'),
    (1, 2, 'CONFIRMED', 'HIGH', 'DB'),
    (1, 3, 'CONFIRMED', 'HIGH', 'DB'),
    (1, 4, 'CONFIRMED', 'HIGH', 'DB'),

    -- 콩나물국: 숨은 재료는 멸치 육수(채식·해산물에서 R2 "확인 필요")
    (2, 5, 'CONFIRMED', 'HIGH', 'DB'),
    (2, 7, 'CONFIRMED', 'HIGH', 'DB'),
    (2, 8, 'CONFIRMED', 'HIGH', 'DB'),
    (2, 6, 'INFERRED', 'MEDIUM', 'COOKING_METHOD'),

    -- 그린샐러드: 걸리는 재료가 없어 채식·할랄에서 R4 "안전"
    (3, 9, 'CONFIRMED', 'HIGH', 'DB'),
    (3, 10, 'CONFIRMED', 'HIGH', 'DB'),
    (3, 11, 'CONFIRMED', 'HIGH', 'DB'),
    (3, 12, 'CONFIRMED', 'HIGH', 'DB'),

    -- 버섯리조또: 유제품은 CONFIRMED(비건에서 R1), 숨은 재료는 치킨 스톡(채식에서 R2)
    (4, 13, 'CONFIRMED', 'HIGH', 'DB'),
    (4, 14, 'CONFIRMED', 'HIGH', 'DB'),
    (4, 15, 'CONFIRMED', 'HIGH', 'DB'),
    (4, 16, 'CONFIRMED', 'HIGH', 'DB'),
    (4, 17, 'INFERRED', 'MEDIUM', 'COOKING_METHOD'),

    -- 김치찌개: 돼지고기가 CONFIRMED라 할랄에서 R1. 숨은 재료는 김치 속 젓갈과 맛술
    (5, 18, 'CONFIRMED', 'HIGH', 'DB'),
    (5, 19, 'CONFIRMED', 'HIGH', 'DB'),
    (5, 20, 'CONFIRMED', 'HIGH', 'DB'),
    (5, 38, 'CONFIRMED', 'HIGH', 'DB'),
    (5, 21, 'INFERRED', 'MEDIUM', 'COOKING_METHOD'),
    (5, 22, 'INFERRED', 'LOW', 'COOKING_METHOD'),

    -- 비빔밥(E1, halal-bibimbap-gochujang): CONFIRMED 중에 pork·alcohol에 걸리는 것이 없고,
    -- 고추장 속 다진 돼지고기만 INFERRED라 할랄에서 R2 "확인 필요"가 나온다
    (6, 23, 'CONFIRMED', 'HIGH', 'DB'),
    (6, 24, 'CONFIRMED', 'HIGH', 'DB'),
    (6, 5, 'CONFIRMED', 'HIGH', 'DB'),
    (6, 25, 'CONFIRMED', 'HIGH', 'DB'),
    (6, 26, 'CONFIRMED', 'HIGH', 'DB'),
    (6, 27, 'CONFIRMED', 'HIGH', 'DB'),
    (6, 28, 'INFERRED', 'MEDIUM', 'COOKING_METHOD'),

    -- 비빔국수(E2, peanut-allergy-bibimguksu): CONFIRMED 중에 peanut에 걸리는 것이 없고,
    -- 양념장 속 땅콩만 INFERRED라 "땅콩" 토글에서 R2 "확인 필요"가 나온다
    (7, 29, 'CONFIRMED', 'HIGH', 'DB'),
    (7, 30, 'CONFIRMED', 'HIGH', 'DB'),
    (7, 11, 'CONFIRMED', 'HIGH', 'DB'),
    (7, 26, 'CONFIRMED', 'HIGH', 'DB'),
    (7, 31, 'INFERRED', 'MEDIUM', 'COOKING_METHOD'),

    -- 부대찌개: 숨은 재료 햄·소시지가 CONFIRMED(할랄에서 R1), 사골 육수는 INFERRED
    (8, 32, 'CONFIRMED', 'HIGH', 'DB'),
    (8, 33, 'CONFIRMED', 'HIGH', 'DB'),
    (8, 19, 'CONFIRMED', 'HIGH', 'DB'),
    (8, 34, 'CONFIRMED', 'HIGH', 'DB'),
    (8, 35, 'INFERRED', 'MEDIUM', 'COOKING_METHOD'),

    -- 떡볶이: 숨은 재료 어묵이 CONFIRMED(해산물에서 R1), 멸치 육수는 INFERRED
    (9, 36, 'CONFIRMED', 'HIGH', 'DB'),
    (9, 37, 'CONFIRMED', 'HIGH', 'DB'),
    (9, 27, 'CONFIRMED', 'HIGH', 'DB'),
    (9, 6, 'INFERRED', 'MEDIUM', 'COOKING_METHOD');
