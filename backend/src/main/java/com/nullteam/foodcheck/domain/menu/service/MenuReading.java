package com.nullteam.foodcheck.domain.menu.service;

import java.util.List;

/**
 * AI가 사진을 읽은 결과(구조화 출력). API 응답이 아니라 MenuService 안에서만 쓴다.
 * 읽지 못한 메뉴는 빈 문자열이나 null로 온다.
 */
public record MenuReading(Kind kind, List<String> menus) {

	public enum Kind {
		/** 메뉴판: 적힌 메뉴 전부 */
		MENU_BOARD,
		/** 음식 사진: 요리명 1개 */
		FOOD,
		/** 메뉴판도 음식 사진도 아니다 → 422 */
		NOT_MENU
	}
}
