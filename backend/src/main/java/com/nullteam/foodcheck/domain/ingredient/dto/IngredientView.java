package com.nullteam.foodcheck.domain.ingredient.dto;

import java.util.List;

/**
 * 재료 하나와 그 근거(REQ-6). REQ-3·OPT-1 응답의 ingredients[]와 REQ-5 응답이 같은 형식을 쓴다.
 *
 * @param restrictions 재료에 정해진 제한 항목 코드 전부(사용자와 상관없다). 걸리는 항목이 없으면 빈 목록
 */
public record IngredientView(
		String name,
		Evidence evidence,
		Confidence confidence,
		Basis basis,
		List<String> restrictions) {
}
