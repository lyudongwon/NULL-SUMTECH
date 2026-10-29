package com.nullteam.foodcheck.domain.ingredient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nullteam.foodcheck.domain.ingredient.entity.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

	Optional<Ingredient> findByName(String name);
}
