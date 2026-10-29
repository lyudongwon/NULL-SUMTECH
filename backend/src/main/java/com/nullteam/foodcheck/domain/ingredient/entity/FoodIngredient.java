package com.nullteam.foodcheck.domain.ingredient.entity;

import com.nullteam.foodcheck.domain.ingredient.dto.Basis;
import com.nullteam.foodcheck.domain.ingredient.dto.Confidence;
import com.nullteam.foodcheck.domain.ingredient.dto.Evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * 음식에 들어 있는 재료 하나와 그 근거(REQ-6). food_ingredients 한 행이다.
 * 시드 재료는 CONFIRMED + DB고, 추론으로 찾은 재료는 모두 INFERRED다.
 */
@Embeddable
public class FoodIngredient {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ingredient_id", nullable = false)
	private Ingredient ingredient;

	@Enumerated(EnumType.STRING)
	@Column(name = "evidence")
	private Evidence evidence;

	@Enumerated(EnumType.STRING)
	@Column(name = "confidence")
	private Confidence confidence;

	@Enumerated(EnumType.STRING)
	@Column(name = "basis")
	private Basis basis;

	protected FoodIngredient() {
	}

	private FoodIngredient(Ingredient ingredient, Evidence evidence, Confidence confidence, Basis basis) {
		this.ingredient = ingredient;
		this.evidence = evidence;
		this.confidence = confidence;
		this.basis = basis;
	}

	public static FoodIngredient of(Ingredient ingredient, Evidence evidence, Confidence confidence, Basis basis) {
		return new FoodIngredient(ingredient, evidence, confidence, basis);
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public Evidence getEvidence() {
		return evidence;
	}

	public Confidence getConfidence() {
		return confidence;
	}

	public Basis getBasis() {
		return basis;
	}
}
