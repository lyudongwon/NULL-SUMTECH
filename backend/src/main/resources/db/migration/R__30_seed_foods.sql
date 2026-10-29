-- 음식·재료 시드. 이 파일의 주인은 역할 ③이다(docs/roles.md).
-- TODO(③): erd.md "시드 데이터 최소 요구"의 음식(설렁탕, 콩나물국, 그린샐러드, 버섯리조또, 김치찌개)과
-- 숨은 재료(육수, 젓갈, 맛술, 햄, 어묵 등)를 foods·ingredients·food_ingredients·ingredient_restriction_items에 넣는다.
-- 여러 번 실행돼도 결과가 같게(ON CONFLICT) 쓴다.
SELECT 1;
