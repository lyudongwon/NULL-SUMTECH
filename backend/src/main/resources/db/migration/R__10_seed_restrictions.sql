-- REQ-4 제한 항목·프리셋 시드(역할 ①). 내용이 바뀌면 Flyway가 다시 실행하므로 여러 번 실행돼도 결과가 같게 쓴다.
-- 코드는 docs/api.md > 열거형, 화면 이름은 영어다(docs/api.md > 화면 문구, REQ-8).

INSERT INTO restriction_items (code, name) VALUES
    ('meat', 'Meat'),
    ('pork', 'Pork'),
    ('beef', 'Beef'),
    ('seafood', 'Seafood'),
    ('egg', 'Egg'),
    ('dairy', 'Dairy'),
    ('peanut', 'Peanut'),
    ('alcohol', 'Alcohol')
ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name;

INSERT INTO restriction_presets (code, name) VALUES
    ('HALAL', 'Halal'),
    ('VEGETARIAN', 'Vegetarian'),
    ('VEGAN', 'Vegan')
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
