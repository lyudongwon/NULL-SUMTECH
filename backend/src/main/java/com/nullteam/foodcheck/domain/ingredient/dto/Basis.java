package com.nullteam.foodcheck.domain.ingredient.dto;

/** docs/api.md 열거형 basis. REQ-5가 재료를 어디서 찾았는지다. */
public enum Basis {
	DB,
	NAME_KEYWORD,
	COOKING_METHOD,
	RECIPE_SEARCH,
	AI_ESTIMATE
}
