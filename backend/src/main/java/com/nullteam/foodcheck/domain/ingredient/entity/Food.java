package com.nullteam.foodcheck.domain.ingredient.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/** 음식 DB. source는 시드(SEED)인지 REQ-5가 찾아 넣은 것(RECIPE_SEARCH·AI_ESTIMATE)인지 구분한다. */
@Entity
@Table(name = "foods")
public class Food {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String source;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "food_ingredients", joinColumns = @JoinColumn(name = "food_id"))
	private List<FoodIngredient> ingredients = new ArrayList<>();

	protected Food() {
	}

	private Food(String name, String source, List<FoodIngredient> ingredients) {
		this.name = name;
		this.source = source;
		this.ingredients = new ArrayList<>(ingredients);
	}

	/** @param source docs/erd.md foods.source. REQ-5가 찾아 넣을 때는 RECIPE_SEARCH 또는 AI_ESTIMATE */
	public static Food of(String name, String source, List<FoodIngredient> ingredients) {
		return new Food(name, source, ingredients);
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public List<FoodIngredient> getIngredients() {
		return ingredients;
	}
}
