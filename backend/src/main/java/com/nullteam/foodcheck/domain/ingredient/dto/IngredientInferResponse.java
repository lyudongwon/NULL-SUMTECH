package com.nullteam.foodcheck.domain.ingredient.dto;

import java.util.List;

/** POST /api/v1/ingredients/infer 응답 (REQ-5). 재료를 하나도 찾지 못하면 ingredients가 빈 목록이다. */
public record IngredientInferResponse(String menuName, List<IngredientView> ingredients) {
}
