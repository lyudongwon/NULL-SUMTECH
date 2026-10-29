package com.nullteam.foodcheck.domain.judgment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.nullteam.foodcheck.domain.ingredient.dto.Basis;
import com.nullteam.foodcheck.domain.ingredient.dto.Confidence;
import com.nullteam.foodcheck.domain.ingredient.dto.Evidence;
import com.nullteam.foodcheck.domain.ingredient.dto.IngredientView;
import com.nullteam.foodcheck.domain.judgment.dto.JudgmentResult;
import com.nullteam.foodcheck.domain.judgment.dto.Level;
import com.nullteam.foodcheck.domain.judgment.dto.Rule;

/** REQ-3 판정 규칙 R0~R4 (docs/api.md). 규칙마다 테스트를 둔다(docs/ai-rules.md 7번). */
class JudgmentRulesTest {

	private static final Map<String, String> ITEM_NAMES = Map.of(
			"meat", "Meat", "pork", "Pork", "beef", "Beef", "seafood", "Seafood",
			"egg", "Egg", "dairy", "Dairy", "peanut", "Peanut", "alcohol", "Alcohol");

	private static final Set<String> HALAL = Set.of("pork", "alcohol");
	private static final Set<String> VEGETARIAN = Set.of("meat", "seafood");

	private static IngredientView confirmed(String name, String... restrictions) {
		return new IngredientView(name, Evidence.CONFIRMED, Confidence.HIGH, Basis.DB, List.of(restrictions));
	}

	private static IngredientView inferred(String name, Basis basis, String... restrictions) {
		return new IngredientView(name, Evidence.INFERRED, Confidence.MEDIUM, basis, List.of(restrictions));
	}

	private static JudgmentResult judge(String menuName, Set<String> enabled, IngredientView... ingredients) {
		return JudgmentRules.apply(menuName, enabled, List.of(ingredients), ITEM_NAMES);
	}

	@Test
	void R0_제한이_하나도_없으면_걸리는_재료가_있어도_안전이다() {
		JudgmentResult result = judge("설렁탕", Set.of(), confirmed("소고기", "beef", "meat"));

		assertEquals(Level.SAFE, result.level());
		assertEquals(Rule.R0, result.rule());
		assertEquals(List.of(), result.matchedRestrictions());
	}

	@Test
	void R0_사용자를_찾지_못해_제한이_null이어도_안전이다() {
		JudgmentResult result = JudgmentRules.apply("설렁탕", null,
				List.of(confirmed("소고기", "beef", "meat")), ITEM_NAMES);

		assertEquals(Level.SAFE, result.level());
		assertEquals(Rule.R0, result.rule());
	}

	@Test
	void R1_확인된_재료가_제한에_걸리면_불가다() {
		JudgmentResult result = judge("설렁탕", VEGETARIAN,
				confirmed("소고기", "beef", "meat"),
				confirmed("대파"));

		assertEquals(Level.BLOCKED, result.level());
		assertEquals(Rule.R1, result.rule());
		// 걸린 항목만 담는다. 재료의 beef는 사용자가 켜지 않았으므로 빠진다
		assertEquals(List.of("meat"), result.matchedRestrictions());
		assertEquals("Contains 소고기, which is not allowed for your Meat restriction.", result.reason());
	}

	@Test
	void R1_확인과_추정이_함께_걸리면_확인_쪽을_따라_불가다() {
		JudgmentResult result = judge("김치찌개", HALAL,
				confirmed("돼지고기", "pork", "meat"),
				inferred("맛술", Basis.COOKING_METHOD, "alcohol"));

		assertEquals(Level.BLOCKED, result.level());
		assertEquals(Rule.R1, result.rule());
		assertEquals(List.of("pork"), result.matchedRestrictions());
	}

	@Test
	void R2_추정_재료만_걸리면_확인_필요이고_matched_restrictions는_비운다() {
		JudgmentResult result = judge("비빔밥", HALAL,
				confirmed("쌀밥"),
				confirmed("고추장"),
				inferred("고추장 속 다진 돼지고기", Basis.COOKING_METHOD, "pork", "meat"));

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R2, result.rule());
		assertEquals(List.of(), result.matchedRestrictions());
		assertEquals("May contain 고추장 속 다진 돼지고기. Check for Pork before eating.", result.reason());
	}

	@Test
	void R2_AI_추정_재료가_걸려도_불가가_아니라_확인_필요다() {
		JudgmentResult result = judge("된장찌개", HALAL,
				inferred("맛술", Basis.AI_ESTIMATE, "alcohol"));

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R2, result.rule());
	}

	@Test
	void R3_걸리는_재료가_없고_모두_AI_추정이면_확인_필요다() {
		JudgmentResult result = judge("된장찌개", HALAL,
				inferred("된장", Basis.AI_ESTIMATE),
				inferred("두부", Basis.AI_ESTIMATE));

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R3, result.rule());
	}

	@Test
	void R3_AI_추정에_조리법_추정이_섞여도_DB나_레시피_재료가_없으면_확인_필요다() {
		// DB·만개의 레시피에 없는 "두부조림": AI가 [두부, 간장], 조리법 "조림"이 맛술을 더한 경우
		JudgmentResult result = judge("두부조림", Set.of("peanut"),
				inferred("두부", Basis.AI_ESTIMATE),
				inferred("간장", Basis.AI_ESTIMATE),
				inferred("맛술", Basis.COOKING_METHOD, "alcohol"));

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R3, result.rule());
	}

	@Test
	void R3_AI가_실패해_이름_단어_재료만_남아도_확인_필요다() {
		JudgmentResult result = judge("치즈떡볶이", Set.of("peanut"),
				inferred("치즈", Basis.NAME_KEYWORD, "dairy"));

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R3, result.rule());
	}

	@Test
	void R4_만개의_레시피에서_찾은_재료가_있고_걸리지_않으면_안전이다() {
		JudgmentResult result = judge("된장찌개", HALAL,
				inferred("된장", Basis.RECIPE_SEARCH),
				inferred("두부", Basis.RECIPE_SEARCH),
				inferred("애호박", Basis.NAME_KEYWORD));

		assertEquals(Level.SAFE, result.level());
		assertEquals(Rule.R4, result.rule());
	}

	@Test
	void R3_재료를_하나도_찾지_못하면_확인_필요다() {
		JudgmentResult result = judge("처음 보는 메뉴", HALAL);

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R3, result.rule());
	}

	@Test
	void R4_걸리는_재료가_없으면_안전이다() {
		JudgmentResult result = judge("그린샐러드", VEGETARIAN,
				confirmed("양상추"),
				confirmed("방울토마토"),
				inferred("발사믹 드레싱", Basis.COOKING_METHOD));

		assertEquals(Level.SAFE, result.level());
		assertEquals(Rule.R4, result.rule());
		assertEquals(List.of(), result.matchedRestrictions());
	}

	@Test
	void R4_AI_추정이_섞여_있어도_DB_재료가_있으면_안전이다() {
		JudgmentResult result = judge("그린샐러드", VEGETARIAN,
				confirmed("양상추"),
				inferred("올리브유", Basis.AI_ESTIMATE));

		assertEquals(Level.SAFE, result.level());
		assertEquals(Rule.R4, result.rule());
	}

	@Test
	void 근거_상세는_걸리지_않는_재료까지_모두_그대로_돌려준다() {
		IngredientView beef = confirmed("소고기", "beef", "meat");
		IngredientView onion = confirmed("대파");
		JudgmentResult result = judge("설렁탕", VEGETARIAN, beef, onion);

		assertEquals(List.of(beef, onion), result.ingredients());
	}

	@Test
	void E2_땅콩_토글만_켜도_양념장_속_땅콩으로_확인_필요가_나온다() {
		JudgmentResult result = judge("비빔국수", Set.of("peanut"),
				confirmed("소면"),
				confirmed("양념장"),
				confirmed("계란", "egg"),
				inferred("양념장 속 땅콩", Basis.COOKING_METHOD, "peanut"));

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R2, result.rule());
		assertTrue(result.reason().contains("양념장"), result.reason());
		assertTrue(result.reason().contains("땅콩"), result.reason());
	}
}
