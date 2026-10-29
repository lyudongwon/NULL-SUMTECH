package com.nullteam.foodcheck.domain.ingredient.entity;

import java.util.Set;
import java.util.TreeSet;

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

/** 재료 사전. restrictionItems가 비어 있으면 어느 제한에도 걸리지 않는다는 뜻이다. */
@Entity
@Table(name = "ingredients")
public class Ingredient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	/** 이 재료가 걸리는 제한 항목 코드 전부(돼지고기는 pork와 meat). docs/api.md 열거형. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "ingredient_restriction_items", joinColumns = @JoinColumn(name = "ingredient_id"))
	@Column(name = "item_code")
	private Set<String> restrictionItems = new TreeSet<>();

	protected Ingredient() {
	}

	private Ingredient(String name, Set<String> restrictionItems) {
		this.name = name;
		this.restrictionItems = new TreeSet<>(restrictionItems);
	}

	/** REQ-5가 만개의 레시피·AI 추정으로 처음 찾은 재료를 사전에 넣을 때 쓴다. */
	public static Ingredient of(String name, Set<String> restrictionItems) {
		return new Ingredient(name, restrictionItems);
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<String> getRestrictionItems() {
		return restrictionItems;
	}
}
