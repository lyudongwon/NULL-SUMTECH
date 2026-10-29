package com.nullteam.foodcheck.domain.ingredient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** REQ-5 3단계. 만개의 레시피 응답에서 재료 이름만 뽑아내는 부분을 본다(네트워크는 쓰지 않는다). */
class RecipeSearchClientTest {

	private final RecipeSearchClient client = new RecipeSearchClient(true);

	@Test
	void 분량을_뗀다() {
		assertEquals("느타리버섯", RecipeSearchClient.cleanName("느타리버섯 1줌"));
		assertEquals("고추가루", RecipeSearchClient.cleanName("고추가루 1.5T"));
		assertEquals("물", RecipeSearchClient.cleanName("물 1.5종이컵"));
	}

	@Test
	void 숫자_없는_분량_표현도_뗀다() {
		assertEquals("소금", RecipeSearchClient.cleanName("소금 간맞추기 "));
		assertEquals("고추", RecipeSearchClient.cleanName("고추 기호에맞게"));
		assertEquals("참기름", RecipeSearchClient.cleanName("참기름 약간"));
	}

	@Test
	void 괄호_설명을_뗀다() {
		assertEquals("양상추", RecipeSearchClient.cleanName("양상추 (곁들일양)"));
	}

	@Test
	void 두_낱말짜리_재료_이름은_그대로_둔다() {
		assertEquals("멸치 육수", RecipeSearchClient.cleanName("멸치 육수 2컵"));
		assertEquals("다진 마늘", RecipeSearchClient.cleanName("다진 마늘"));
	}

	@Test
	void 이름으로_보기_어려우면_버린다() {
		assertEquals("", RecipeSearchClient.cleanName("1T"));
		assertEquals("", RecipeSearchClient.cleanName("취향껏"));
		assertEquals("", RecipeSearchClient.cleanName("아주아주 길고 긴 설명이 붙은 재료 이름입니다"));
	}

	@Test
	void JSON_LD의_recipeIngredient에서_재료를_뽑는다() {
		String html = """
				<html><head>
				<script type="application/ld+json">
				{"@type":"Recipe","name":"된장찌개",
				 "recipeIngredient":["느타리버섯 1줌","두부 1/4모","된장 2T","소금 간맞추기 "]}
				</script>
				</head><body>...</body></html>
				""";

		assertEquals(List.of("느타리버섯", "두부", "된장", "소금"), client.parseIngredients(html));
	}

	@Test
	void JSON_LD가_없거나_깨졌으면_빈_목록이다() {
		assertTrue(client.parseIngredients("<html><body>재료 없음</body></html>").isEmpty());
		assertTrue(client.parseIngredients(
				"<script type=\"application/ld+json\">{깨진 JSON</script>").isEmpty());
	}

	@Test
	void 꺼_두면_검색하지_않고_빈_목록을_돌려준다() {
		assertTrue(new RecipeSearchClient(false).search("된장찌개").isEmpty());
	}

	@Test
	void 레시피_제목에_메뉴_이름이_있어야_쓴다() {
		String html = """
				<script type="application/ld+json">{"@type":"Recipe","name":"초간단 된장 찌개 끓이는 법","recipeIngredient":["된장 2T"]}</script>
				""";

		assertTrue(client.titleMatches(html, "된장찌개"));
		assertFalse(client.titleMatches(html, "ㅁㄴㅇㄹ"));
		assertFalse(client.titleMatches(html, "아이스 카페 아메리카노"));
		assertFalse(client.titleMatches("<html></html>", "된장찌개"));
	}
}
