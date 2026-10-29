package com.nullteam.foodcheck.domain.menu.dto;

/** REQ-2 판독한 사진 종류 (docs/api.md 열거형 image_type). */
public enum ImageType {
	/** 메뉴판: 적힌 메뉴 전부 */
	MENU_BOARD,
	/** 음식 사진: menus 1개 */
	FOOD
}
