package com.nullteam.foodcheck.domain.ingredient.service;

/**
 * 판독·입력으로 들어온 메뉴 이름을 음식 DB에서 찾을 수 있는 모양으로 다듬는다.
 * TODO(확인): 동의어(돈까스/돈가스)와 표기 차이를 어디까지 맞출지는 docs/erd.md 열린 질문 5번이다.
 * 지금은 앞뒤 공백과 겹친 공백만 정리하고, 띄어쓰기 차이는 FoodRepository.findByCompactName이 맡는다.
 */
public final class MenuNameNormalizer {

	private MenuNameNormalizer() {
	}

	public static String normalize(String menuName) {
		return menuName == null ? "" : menuName.strip().replaceAll("\\s+", " ");
	}

	/** 띄어쓰기를 모두 뺀 이름. "제육 볶음"과 "제육볶음"을 같게 보려고 쓴다. */
	public static String compact(String menuName) {
		return normalize(menuName).replace(" ", "");
	}
}
