package com.nullteam.foodcheck.domain.ingredient.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.ingredient.dto.IngredientInferRequest;
import com.nullteam.foodcheck.domain.ingredient.dto.IngredientInferResponse;
import com.nullteam.foodcheck.domain.ingredient.service.IngredientInferenceService;

/** REQ-5 숨은 성분 추론 (docs/api.md). 판정은 서버 안에서 같은 서비스를 부르고, 이 API는 확인용이다. */
@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientController {

	private final IngredientInferenceService ingredientInferenceService;

	public IngredientController(IngredientInferenceService ingredientInferenceService) {
		this.ingredientInferenceService = ingredientInferenceService;
	}

	@PostMapping("/infer")
	public IngredientInferResponse infer(@RequestBody(required = false) IngredientInferRequest request) {
		return ingredientInferenceService.inferResponse(request == null ? null : request.menuName());
	}
}
