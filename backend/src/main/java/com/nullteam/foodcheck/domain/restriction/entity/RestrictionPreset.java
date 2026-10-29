package com.nullteam.foodcheck.domain.restriction.entity;

import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/** 프리셋(HALAL, VEGETARIAN, VEGAN). 고르면 items의 항목이 켜진다. */
@Entity
@Table(name = "restriction_presets")
public class RestrictionPreset {

	@Id
	private String code;

	@Column(nullable = false)
	private String name;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "preset_items", joinColumns = @JoinColumn(name = "preset_code"))
	@Column(name = "item_code")
	private Set<String> items = new TreeSet<>();

	protected RestrictionPreset() {
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public Set<String> getItems() {
		return items;
	}
}
