package com.nullteam.foodcheck.domain.judgment.dto;

/** docs/api.md 판정 규칙. 위에서부터 처음 맞는 규칙 하나를 쓰고, 어떤 규칙이 적용됐는지 응답에 담는다(REQ-6). */
public enum Rule {
	/** 사용자 제한이 하나도 없다(미설정·미등록 user_id 포함) → SAFE */
	R0,
	/** CONFIRMED 재료가 제한에 걸린다 → BLOCKED */
	R1,
	/** INFERRED 재료가 제한에 걸린다 → CHECK */
	R2,
	/** 걸리는 재료는 없지만 음식 DB·만개의 레시피에서 온 재료가 하나도 없다(AI·이름 단어·조리법 추정뿐이거나 못 찾음) → CHECK */
	R3,
	/** 걸리는 재료가 없다 → SAFE */
	R4
}
