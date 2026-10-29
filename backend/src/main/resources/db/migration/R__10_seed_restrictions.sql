-- REQ-4 제한 항목·프리셋 시드(역할 ①). 내용이 바뀌면 Flyway가 다시 실행하므로 여러 번 실행돼도 결과가 같게 쓴다.
-- 코드는 docs/api.md > 열거형, 화면 이름은 docs/scenarios.yaml("채식"·"할랄"·"고기")을 따른다.

INSERT INTO restriction_items (code, name) VALUES
    ('meat', '고기'),
    ('pork', '돼지고기'),
    ('beef', '소고기'),
    ('seafood', '해산물'),
    ('egg', '계란'),
    ('dairy', '우유·유제품'),
    ('peanut', '땅콩'),
    ('alcohol', '술')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name;

INSERT INTO restriction_presets (code, name) VALUES
    ('HALAL', '할랄'),
    ('VEGETARIAN', '채식'),
    ('VEGAN', '비건')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name;

DELETE FROM preset_items WHERE preset_code IN ('HALAL', 'VEGETARIAN', 'VEGAN');
INSERT INTO preset_items (preset_code, item_code) VALUES
    ('HALAL', 'pork'),
    ('HALAL', 'alcohol'),
    ('VEGETARIAN', 'meat'),
    ('VEGETARIAN', 'seafood'),
    ('VEGAN', 'meat'),
    ('VEGAN', 'seafood'),
    ('VEGAN', 'egg'),
    ('VEGAN', 'dairy');
