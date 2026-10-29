package com.nullteam.foodcheck.domain.ingredient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

/** REQ-5 2단계. 메뉴 이름 속 재료 단어·조리법과, 사전에 없는 재료 이름의 제한 항목을 본다. */
class IngredientKeywordsTest {

	@Test
	void 이름_속_재료_단어를_찾는다() {
		assertTrue(IngredientKeywords.fromMenuName("돈가스").contains("돼지고기"));
		assertTrue(IngredientKeywords.fromMenuName("제육볶음").contains("돼지고기"));
		assertTrue(IngredientKeywords.fromMenuName("설렁탕").contains("소고기"));
		assertTrue(IngredientKeywords.fromMenuName("닭갈비").contains("닭고기"));
	}

	@Test
	void 이름에_재료_단어가_없으면_비운다() {
		assertTrue(IngredientKeywords.fromMenuName("그린샐러드").isEmpty());
	}

	@Test
	void 조리법으로_튀김옷_재료를_찾는다() {
		assertEquals(Set.of("밀가루", "계란"), IngredientKeywords.fromCookingMethod("새우튀김"));
	}

	@Test
	void 조리법으로_맛술을_찾는다() {
		assertEquals(Set.of("맛술"), IngredientKeywords.fromCookingMethod("고등어조림"));
	}

	@Test
	void 돼지고기는_pork와_meat에_모두_걸린다() {
		assertEquals(Set.of("pork", "meat"), IngredientKeywords.restrictionsOf("돼지고기앞다리살"));
		assertEquals(Set.of("pork", "meat"), IngredientKeywords.restrictionsOf("햄"));
	}

	@Test
	void 소고기는_beef와_meat에_모두_걸린다() {
		assertEquals(Set.of("beef", "meat"), IngredientKeywords.restrictionsOf("사골 육수"));
	}

	@Test
	void 숨은_재료_단어를_제한_항목으로_옮긴다() {
		assertEquals(Set.of("seafood"), IngredientKeywords.restrictionsOf("멸치 액젓"));
		assertEquals(Set.of("seafood"), IngredientKeywords.restrictionsOf("어묵"));
		assertEquals(Set.of("alcohol"), IngredientKeywords.restrictionsOf("맛술"));
		assertEquals(Set.of("egg"), IngredientKeywords.restrictionsOf("마요네즈"));
		assertEquals(Set.of("dairy"), IngredientKeywords.restrictionsOf("파마산 치즈"));
		assertEquals(Set.of("peanut"), IngredientKeywords.restrictionsOf("볶은 땅콩"));
	}

	@Test
	void 미역과_다시마는_해산물로_보지_않는다() {
		// 해조류다. docs/api.md REQ-3 예시도 미역의 restrictions를 비워 두었다.
		assertTrue(IngredientKeywords.restrictionsOf("미역").isEmpty());
		assertTrue(IngredientKeywords.restrictionsOf("다시마").isEmpty());
	}

	@Test
	void 걸리지_않는_재료는_빈_집합이다() {
		assertTrue(IngredientKeywords.restrictionsOf("양상추").isEmpty());
		assertTrue(IngredientKeywords.restrictionsOf("고추장").isEmpty());
		assertTrue(IngredientKeywords.restrictionsOf("두부").isEmpty());
	}
}
