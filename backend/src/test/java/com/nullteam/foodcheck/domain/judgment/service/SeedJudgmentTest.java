package com.nullteam.foodcheck.domain.judgment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.nullteam.foodcheck.domain.ingredient.dto.Basis;
import com.nullteam.foodcheck.domain.ingredient.dto.Confidence;
import com.nullteam.foodcheck.domain.ingredient.dto.Evidence;
import com.nullteam.foodcheck.domain.ingredient.dto.IngredientView;
import com.nullteam.foodcheck.domain.ingredient.service.IngredientKeywords;
import com.nullteam.foodcheck.domain.judgment.dto.JudgmentResult;
import com.nullteam.foodcheck.domain.judgment.dto.Level;
import com.nullteam.foodcheck.domain.judgment.dto.Rule;

/**
 * Flyway 시드(R__10·R__30)를 그대로 읽어 판정 규칙(JudgmentRules)에 넣었을 때 docs/scenarios.yaml의
 * expected와 같은 판정이 나오는지 본다. 시드 값이 바뀌어 시연 흐름이 깨지면 여기서 먼저 실패한다.
 *
 * <p>이 PC·CI 단위 테스트에는 PostgreSQL이 없으므로 SQL의 VALUES 줄만 읽는다. DB 조회 경로는
 * 흐름 E2E(e2e/scenarios)가 확인한다.
 */
class SeedJudgmentTest {

	private static final Pattern INSERT = Pattern.compile(
			"INSERT INTO (\\w+) \\([^)]*\\) VALUES(.*?);", Pattern.DOTALL);
	private static final Pattern ROW = Pattern.compile("\\(([^()]*)\\)");

	private static final Map<String, String> ITEM_NAMES = new LinkedHashMap<>();
	private static final Map<String, Set<String>> PRESETS = new LinkedHashMap<>();
	private static final Map<Integer, String> INGREDIENT_NAMES = new LinkedHashMap<>();
	private static final Map<Integer, Set<String>> INGREDIENT_CODES = new LinkedHashMap<>();
	private static final Map<String, Integer> FOOD_IDS = new LinkedHashMap<>();
	private static final Map<Integer, List<IngredientView>> FOOD_INGREDIENTS = new LinkedHashMap<>();

	@BeforeAll
	static void loadSeed() throws IOException {
		for (List<String> row : rows(read("R__10_seed_restrictions.sql"), "restriction_items")) {
			ITEM_NAMES.put(row.get(0), row.get(1));
		}
		for (List<String> row : rows(read("R__10_seed_restrictions.sql"), "preset_items")) {
			PRESETS.computeIfAbsent(row.get(0), k -> new TreeSet<>()).add(row.get(1));
		}

		String foods = read("R__30_seed_foods.sql");
		for (List<String> row : rows(foods, "ingredients")) {
			INGREDIENT_NAMES.put(Integer.valueOf(row.get(0)), row.get(1));
		}
		for (List<String> row : rows(foods, "ingredient_restriction_items")) {
			INGREDIENT_CODES.computeIfAbsent(Integer.valueOf(row.get(0)), k -> new TreeSet<>()).add(row.get(1));
		}
		for (List<String> row : rows(foods, "foods")) {
			FOOD_IDS.put(row.get(1), Integer.valueOf(row.get(0)));
		}
		for (List<String> row : rows(foods, "food_ingredients")) {
			int ingredientId = Integer.parseInt(row.get(1));
			IngredientView view = new IngredientView(INGREDIENT_NAMES.get(ingredientId),
					Evidence.valueOf(row.get(2)), Confidence.valueOf(row.get(3)), Basis.valueOf(row.get(4)),
					List.copyOf(INGREDIENT_CODES.getOrDefault(ingredientId, Set.of())));
			FOOD_INGREDIENTS.computeIfAbsent(Integer.valueOf(row.get(0)), k -> new ArrayList<>()).add(view);
		}
	}

	private static String read(String file) throws IOException {
		try (InputStream in = SeedJudgmentTest.class.getClassLoader()
				.getResourceAsStream("db/migration/" + file)) {
			assertNotNull(in, "시드 파일이 없다: " + file);
			String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			return sql.replaceAll("--[^\\n]*", "");
		}
	}

	private static List<List<String>> rows(String sql, String table) {
		List<List<String>> rows = new ArrayList<>();
		Matcher insert = INSERT.matcher(sql);
		while (insert.find()) {
			if (!insert.group(1).equals(table)) {
				continue;
			}
			// "ON CONFLICT (code) ..."의 괄호는 값 줄이 아니다.
			Matcher row = ROW.matcher(insert.group(2).split("ON CONFLICT")[0]);
			while (row.find()) {
				List<String> values = new ArrayList<>();
				for (String value : row.group(1).split(",")) {
					values.add(value.trim().replaceAll("^'|'$", ""));
				}
				rows.add(values);
			}
		}
		assertFalse(rows.isEmpty(), "시드에서 " + table + " 줄을 읽지 못했다");
		return rows;
	}

	private static JudgmentResult judge(String menuName, Set<String> enabled) {
		Integer foodId = FOOD_IDS.get(menuName);
		assertNotNull(foodId, "시드에 음식이 없다: " + menuName);
		return JudgmentRules.apply(menuName, enabled, FOOD_INGREDIENTS.get(foodId), ITEM_NAMES);
	}

	@Test
	void 시나리오_예시_메뉴가_모두_시드에_있고_된장찌개는_없다() {
		// docs/scenarios.yaml 머리 주석: 예시 메뉴는 시드에 넣고, 된장찌개는 DB에 없는 경로를 보려고 뺀다.
		for (String menu : List.of("설렁탕", "콩나물국", "그린샐러드", "버섯리조또", "김치찌개", "비빔밥", "비빔국수")) {
			assertTrue(FOOD_IDS.containsKey(menu), menu);
			assertFalse(FOOD_INGREDIENTS.get(FOOD_IDS.get(menu)).isEmpty(), menu);
		}
		assertFalse(FOOD_IDS.containsKey("된장찌개"));
	}

	@Test
	void 프리셋_시드가_api_md_열거형과_같다() {
		assertEquals(Set.of("pork", "alcohol"), PRESETS.get("HALAL"));
		assertEquals(Set.of("meat", "seafood"), PRESETS.get("VEGETARIAN"));
		assertEquals(Set.of("meat", "seafood", "egg", "dairy"), PRESETS.get("VEGAN"));
	}

	@Test
	void scan_menu_board_채식에서_설렁탕은_소고기_때문에_불가다() {
		JudgmentResult result = judge("설렁탕", PRESETS.get("VEGETARIAN"));

		assertEquals(Level.BLOCKED, result.level());
		assertEquals(Rule.R1, result.rule());
		assertTrue(result.reason().contains("소고기"), result.reason());
		assertEquals(List.of("meat"), result.matchedRestrictions());
	}

	@Test
	void scan_menu_board_채식에서_메뉴판_4개가_각각_판정된다() {
		Set<String> vegetarian = PRESETS.get("VEGETARIAN");

		assertEquals(Level.CHECK, judge("콩나물국", vegetarian).level());
		assertEquals(Rule.R4, judge("그린샐러드", vegetarian).rule());
		assertEquals(Level.SAFE, judge("그린샐러드", vegetarian).level());
		assertEquals(Rule.R2, judge("버섯리조또", vegetarian).rule());
	}

	@Test
	void diet_toggle_only_Meat_토글만_켜도_설렁탕은_Meat_제한으로_불가다() {
		JudgmentResult result = judge("설렁탕", Set.of("meat"));

		assertEquals(Level.BLOCKED, result.level());
		assertTrue(result.reason().contains("소고기"), result.reason());
		assertTrue(result.reason().contains("Meat"), result.reason());
	}

	@Test
	void halal_bibimbap_gochujang_할랄에서_비빔밥은_고추장_속_돼지고기로_확인_필요다() {
		JudgmentResult result = judge("비빔밥", PRESETS.get("HALAL"));

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R2, result.rule());
		assertTrue(result.reason().contains("고추장 속 다진 돼지고기"), result.reason());
		assertEquals(List.of(), result.matchedRestrictions());
	}

	@Test
	void peanut_allergy_bibimguksu_땅콩_토글에서_비빔국수는_양념장_속_땅콩으로_확인_필요다() {
		JudgmentResult result = judge("비빔국수", Set.of("peanut"));

		assertEquals(Level.CHECK, result.level());
		assertEquals(Rule.R2, result.rule());
		assertTrue(result.reason().contains("양념장 속 땅콩"), result.reason());
	}

	@Test
	void manual_input_fallback_할랄에서_김치찌개는_돼지고기로_불가다() {
		JudgmentResult result = judge("김치찌개", PRESETS.get("HALAL"));

		assertEquals(Level.BLOCKED, result.level());
		assertEquals(List.of("pork"), result.matchedRestrictions());
	}

	@Test
	void 시드_재료의_제한_항목은_단어표가_찾는_항목을_모두_담는다() {
		// 같은 재료가 시드(음식 DB)에서는 덜 걸리고 단어표(REQ-5 추정)에서는 더 걸리면 경로마다 판정이 달라진다.
		INGREDIENT_NAMES.forEach((id, name) -> {
			Set<String> seeded = INGREDIENT_CODES.getOrDefault(id, Set.of());
			assertTrue(seeded.containsAll(IngredientKeywords.restrictionsOf(name)),
					name + " 시드=" + seeded + " 단어표=" + IngredientKeywords.restrictionsOf(name));
		});
	}
}
