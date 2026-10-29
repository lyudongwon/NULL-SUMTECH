package com.nullteam.foodcheck.domain.ingredient.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

/**
 * 판독·입력으로 들어온 메뉴 이름을 음식 DB에서 찾을 수 있는 모양으로 다듬는다.
 * 동의어(돈까스/돈가스)는 맞추지 않는다. 어디까지 맞출지는 docs/erd.md 열린 질문 5번이다.
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

	/**
	 * 앞에 꾸밈말이 붙은 변형 메뉴의 기본 요리를 시드 이름에서 찾는다. "돌솥비빔밥" → "비빔밥",
	 * "김치비빔국수" → "비빔국수". 한국어 요리 이름은 기본 요리가 뒤에 오므로 이름 끝이 같은 시드 중
	 * 가장 긴 것을 고른다. 이름이 시드와 똑같으면 변형이 아니므로 비운다.
	 */
	public static Optional<String> baseDishOf(String menuName, Collection<String> seedNames) {
		String menu = compact(menuName);
		return seedNames.stream()
				.filter(seed -> {
					String s = compact(seed);
					return !s.isEmpty() && menu.length() > s.length() && menu.endsWith(s);
				})
				.max(Comparator.comparingInt(seed -> compact(seed).length()));
	}
}
