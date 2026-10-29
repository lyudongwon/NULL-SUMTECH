package com.nullteam.foodcheck.domain.restaurant.entity;

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

@Entity
@Table(name = "restaurants")
public class Restaurant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	private String category;

	private String address;

	/** 이 음식점이 충족하는(쓰지 않는) 제한 항목 코드. */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "restaurant_restriction_items", joinColumns = @JoinColumn(name = "restaurant_id"))
	@Column(name = "item_code")
	private Set<String> satisfiedItems = new TreeSet<>();

	protected Restaurant() {
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getAddress() {
		return address;
	}

	public Set<String> getSatisfiedItems() {
		return satisfiedItems;
	}
}
