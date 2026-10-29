package com.nullteam.foodcheck.domain.ingredient.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * REQ-5 2단계(이름 속 재료 단어·조리법)와, 사전에 없는 재료 이름을 제한 항목으로 옮기는 단어표다.
 * TODO(확인): 단어 목록은 ③이 학식 메뉴를 보고 고른 것으로 팀 검토 전이다. 빠진 단어는 재료를
 * 못 찾는 쪽(추정 없음)으로 기울 뿐 잘못된 "불가"를 만들지는 않는다(걸림은 모두 INFERRED라 최대 R2다).
 */
public final class IngredientKeywords {

	private IngredientKeywords() {
	}

	/** 메뉴 이름에 이 단어가 있으면 그 재료가 들어 있다고 본다(basis = NAME_KEYWORD). */
	private static final Map<String, String> NAME_KEYWORDS = new LinkedHashMap<>();

	/** 메뉴 이름에 이 조리법 단어가 있으면 그 재료들이 쓰였다고 본다(basis = COOKING_METHOD). */
	private static final Map<String, List<String>> COOKING_KEYWORDS = new LinkedHashMap<>();

	/** 재료 이름에 이 단어가 있으면 그 제한 항목에 걸린다고 본다. 사전(ingredients)에 없는 이름에만 쓴다. */
	private static final Map<String, Set<String>> RESTRICTION_KEYWORDS = new LinkedHashMap<>();

	static {
		NAME_KEYWORDS.put("돈가스", "돼지고기");
		NAME_KEYWORDS.put("돈까스", "돼지고기");
		NAME_KEYWORDS.put("제육", "돼지고기");
		NAME_KEYWORDS.put("삼겹", "돼지고기");
		NAME_KEYWORDS.put("목살", "돼지고기");
		NAME_KEYWORDS.put("탕수육", "돼지고기");
		NAME_KEYWORDS.put("보쌈", "돼지고기");
		NAME_KEYWORDS.put("족발", "돼지고기");
		NAME_KEYWORDS.put("순대", "돼지고기");
		NAME_KEYWORDS.put("동파육", "돼지고기");
		NAME_KEYWORDS.put("설렁", "소고기");
		NAME_KEYWORDS.put("설렁탕", "소고기");
		NAME_KEYWORDS.put("곰탕", "소고기");
		NAME_KEYWORDS.put("갈비", "소고기");
		NAME_KEYWORDS.put("불고기", "소고기");
		NAME_KEYWORDS.put("육개장", "소고기");
		NAME_KEYWORDS.put("차돌", "소고기");
		NAME_KEYWORDS.put("치킨", "닭고기");
		NAME_KEYWORDS.put("삼계", "닭고기");
		NAME_KEYWORDS.put("닭", "닭고기");
		NAME_KEYWORDS.put("새우", "새우");
		NAME_KEYWORDS.put("오징어", "오징어");
		NAME_KEYWORDS.put("낙지", "낙지");
		NAME_KEYWORDS.put("해물", "해산물");
		NAME_KEYWORDS.put("멸치", "멸치");
		NAME_KEYWORDS.put("고등어", "고등어");
		NAME_KEYWORDS.put("갈치", "갈치");
		NAME_KEYWORDS.put("참치", "참치");
		NAME_KEYWORDS.put("북어", "북어");
		NAME_KEYWORDS.put("황태", "황태");
		NAME_KEYWORDS.put("어묵", "어묵");
		NAME_KEYWORDS.put("조개", "조개");
		NAME_KEYWORDS.put("계란", "계란");
		NAME_KEYWORDS.put("달걀", "계란");
		NAME_KEYWORDS.put("오믈렛", "계란");
		NAME_KEYWORDS.put("치즈", "치즈");
		NAME_KEYWORDS.put("땅콩", "땅콩");

		// 튀김옷에는 밀가루와 계란이 들어간다(docs/api.md REQ-5 돈가스 예시).
		COOKING_KEYWORDS.put("튀김", List.of("밀가루", "계란"));
		COOKING_KEYWORDS.put("돈가스", List.of("밀가루", "계란"));
		COOKING_KEYWORDS.put("돈까스", List.of("밀가루", "계란"));
		COOKING_KEYWORDS.put("탕수", List.of("밀가루", "계란"));
		COOKING_KEYWORDS.put("강정", List.of("밀가루"));
		COOKING_KEYWORDS.put("부침", List.of("밀가루", "계란"));
		// 볶음·조림·구이·찜에는 맛술이 흔히 쓰인다. 술 제한에서 "확인 필요"로 걸러 주려는 것이다.
		COOKING_KEYWORDS.put("볶음", List.of("맛술"));
		COOKING_KEYWORDS.put("조림", List.of("맛술"));
		COOKING_KEYWORDS.put("구이", List.of("맛술"));
		COOKING_KEYWORDS.put("찜", List.of("맛술"));

		pork("돼지", "제육", "삼겹", "목살", "앞다리", "베이컨", "햄", "소시지", "돈가스", "돈까스", "족발", "순대", "라드");
		beef("소고기", "쇠고기", "사골", "소뼈", "우둔", "차돌", "양지", "불고기", "소갈비");
		meat("고기", "닭", "치킨", "오리", "양고기", "칠면조", "미트");
		seafood("멸치", "새우", "오징어", "낙지", "주꾸미", "조개", "바지락", "홍합", "굴", "꽃게", "대게", "게살",
				"생선", "고등어", "갈치", "참치", "연어", "명태", "북어", "황태", "디포리", "어묵", "액젓", "젓갈",
				"가쓰오", "해물", "해산물", "조기", "명란", "창란", "어간장", "쥐포", "오뎅");
		egg("계란", "달걀", "메추리알", "난백", "난황", "마요네즈");
		dairy("우유", "치즈", "버터", "생크림", "크림", "요구르트", "요거트", "연유", "분유", "휘핑", "라떼");
		peanut("땅콩", "피넛");
		alcohol("맛술", "미림", "미향", "청주", "소주", "와인", "맥주", "정종", "요리술", "럼");
	}

	private static void pork(String... words) {
		put(Set.of("pork", "meat"), words);
	}

	private static void beef(String... words) {
		put(Set.of("beef", "meat"), words);
	}

	private static void meat(String... words) {
		put(Set.of("meat"), words);
	}

	private static void seafood(String... words) {
		put(Set.of("seafood"), words);
	}

	private static void egg(String... words) {
		put(Set.of("egg"), words);
	}

	private static void dairy(String... words) {
		put(Set.of("dairy"), words);
	}

	private static void peanut(String... words) {
		put(Set.of("peanut"), words);
	}

	private static void alcohol(String... words) {
		put(Set.of("alcohol"), words);
	}

	private static void put(Set<String> codes, String... words) {
		for (String word : words) {
			RESTRICTION_KEYWORDS.merge(word, codes, (a, b) -> {
				Set<String> both = new TreeSet<>(a);
				both.addAll(b);
				return both;
			});
		}
	}

	/** 메뉴 이름 속 재료 단어로 찾은 재료 이름. */
	public static Set<String> fromMenuName(String menuName) {
		return match(NAME_KEYWORDS.entrySet().stream()
				.filter(e -> menuName.contains(e.getKey()))
				.map(Map.Entry::getValue)
				.toList());
	}

	/** 메뉴 이름 속 조리법으로 찾은 재료 이름. */
	public static Set<String> fromCookingMethod(String menuName) {
		return match(COOKING_KEYWORDS.entrySet().stream()
				.filter(e -> menuName.contains(e.getKey()))
				.flatMap(e -> e.getValue().stream())
				.toList());
	}

	/** 사전에 없는 재료 이름이 걸리는 제한 항목. 하나도 걸리지 않으면 빈 집합. */
	public static Set<String> restrictionsOf(String ingredientName) {
		Set<String> codes = new TreeSet<>();
		RESTRICTION_KEYWORDS.forEach((word, itemCodes) -> {
			if (ingredientName.contains(word)) {
				codes.addAll(itemCodes);
			}
		});
		return codes;
	}

	private static Set<String> match(List<String> names) {
		return new LinkedHashSet<>(names);
	}
}
