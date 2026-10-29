package com.nullteam.foodcheck.domain.ingredient.dto;

/** docs/api.md 열거형 evidence. 음식 DB에 등록된 재료만 CONFIRMED고, 추론으로 찾은 재료는 모두 INFERRED다. */
public enum Evidence {
	CONFIRMED,
	INFERRED
}
