package com.nullteam.foodcheck.domain.judgment.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.nullteam.foodcheck.domain.ingredient.dto.Basis;
import com.nullteam.foodcheck.domain.ingredient.dto.Evidence;
import com.nullteam.foodcheck.domain.ingredient.dto.IngredientView;
import com.nullteam.foodcheck.domain.judgment.dto.JudgmentResult;
import com.nullteam.foodcheck.domain.judgment.dto.Level;
import com.nullteam.foodcheck.domain.judgment.dto.Rule;

/**
 * REQ-3 판정 규칙 R0~R4 (docs/api.md > REQ-3 > 판정 규칙). 위에서부터 처음 맞는 규칙 하나를 쓴다.
 * AI가 아니라 코드가 판정한다. AI는 재료를 찾을 때만 쓴다(REQ-5).
 *
 * <p>상태가 없는 순수 함수라 DB 없이 단위 테스트한다(JudgmentRulesTest).
 */
public final class JudgmentRules {

	/** 근거는 배지 옆에 한 줄로 보이므로 재료 이름을 앞 두 개까지만 적는다. */
	private static final int MAX_REASON_NAMES = 2;

	private JudgmentRules() {
	}

	/**
	 * @param enabledItems 사용자가 켠 제한 항목 코드. 사용자가 없거나 저장한 적 없으면 빈 집합
	 * @param itemNames    제한 항목 코드 → 화면에 쓰는 영어 이름. 없는 코드는 코드를 그대로 쓴다
	 */
	public static JudgmentResult apply(String menuName, Set<String> enabledItems,
			List<IngredientView> ingredients, Map<String, String> itemNames) {
		if (enabledItems == null || enabledItems.isEmpty()) {
			return result(menuName, Level.SAFE, Rule.R0, "You have no diet restrictions set.", List.of(), ingredients);
		}

		List<IngredientView> confirmedHits = hits(ingredients, enabledItems, Evidence.CONFIRMED);
		if (!confirmedHits.isEmpty()) {
			Set<String> matched = matchedCodes(confirmedHits, enabledItems);
			String reason = "Contains " + names(confirmedHits) + ", which is not allowed for your "
					+ labels(matched, itemNames) + " restriction.";
			return result(menuName, Level.BLOCKED, Rule.R1, reason, List.copyOf(matched), ingredients);
		}

		List<IngredientView> inferredHits = hits(ingredients, enabledItems, Evidence.INFERRED);
		if (!inferredHits.isEmpty()) {
			// matched_restrictions는 확인된 재료로 걸린 항목만 담는다(docs/api.md). 추정은 비워 둔다.
			String reason = "May contain " + names(inferredHits) + ". Check for "
					+ labels(matchedCodes(inferredHits, enabledItems), itemNames) + " before eating.";
			return result(menuName, Level.CHECK, Rule.R2, reason, List.of(), ingredients);
		}

		if (ingredients.isEmpty()) {
			return result(menuName, Level.CHECK, Rule.R3, "No ingredients were found. Please check before eating.", List.of(),
					ingredients);
		}
		// 이름 단어·조리법 추정은 AI 추정 결과에도 덧붙는다(IngredientInferenceService). 그래서 "모두 AI 추정"을
		// "음식 DB나 만개의 레시피로 재료를 하나도 확인하지 못함"으로 본다. 그래야 AI 추정만으로는 늘 CHECK다(REQ-5).
		if (ingredients.stream().noneMatch(i -> i.basis() == Basis.DB || i.basis() == Basis.RECIPE_SEARCH)) {
			return result(menuName, Level.CHECK, Rule.R3, "Ingredients are only estimated, not from the food database or 10000recipe. Please check before eating.", List.of(),
					ingredients);
		}
		return result(menuName, Level.SAFE, Rule.R4, "No ingredients conflict with your diet restrictions.", List.of(),
				ingredients);
	}

	private static List<IngredientView> hits(List<IngredientView> ingredients, Set<String> enabledItems,
			Evidence evidence) {
		return ingredients.stream()
				.filter(i -> i.evidence() == evidence)
				.filter(i -> i.restrictions().stream().anyMatch(enabledItems::contains))
				.toList();
	}

	private static Set<String> matchedCodes(List<IngredientView> hits, Set<String> enabledItems) {
		Set<String> codes = new TreeSet<>();
		for (IngredientView hit : hits) {
			hit.restrictions().stream().filter(enabledItems::contains).forEach(codes::add);
		}
		return codes;
	}

	private static String names(List<IngredientView> hits) {
		List<String> all = List.copyOf(new LinkedHashSet<>(hits.stream().map(IngredientView::name).toList()));
		if (all.size() <= MAX_REASON_NAMES) {
			return String.join(", ", all);
		}
		return String.join(", ", all.subList(0, MAX_REASON_NAMES)) + " and more";
	}

	private static String labels(Set<String> codes, Map<String, String> itemNames) {
		return String.join(", ", codes.stream()
				.map(code -> itemNames == null ? code : itemNames.getOrDefault(code, code))
				.toList());
	}

	private static JudgmentResult result(String menuName, Level level, Rule rule, String reason,
			List<String> matched, List<IngredientView> ingredients) {
		return new JudgmentResult(menuName, level, reason, matched, rule, List.copyOf(ingredients));
	}
}
