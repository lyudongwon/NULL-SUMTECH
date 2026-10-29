package com.nullteam.foodcheck.domain.ingredient.dto;

/** POST /api/v1/ingredients/infer 요청 (REQ-5). */
public record IngredientInferRequest(String menuName) {
}
