package com.nullteam.foodcheck.domain.judgment.dto;

import java.util.List;

import com.nullteam.foodcheck.domain.ingredient.dto.IngredientView;

/**
 * 메뉴 하나의 판정 결과. POST /api/v1/judgments/manual의 응답 본문이고,
 * POST /api/v1/judgments 응답에서는 results[]의 한 항목이다.
 *
 * @param reason              배지 옆에 보이는 근거 한 줄
 * @param matchedRestrictions CONFIRMED 재료로 걸린 제한 항목(BLOCKED의 근거). 그 밖에는 빈 목록
 * @param rule                적용된 판정 규칙 R0~R4 (REQ-6)
 * @param ingredients         근거 상세로 보여 줄 재료 목록 (REQ-6)
 */
public record JudgmentResult(
		String menuName,
		Level level,
		String reason,
		List<String> matchedRestrictions,
		Rule rule,
		List<IngredientView> ingredients) {
}
