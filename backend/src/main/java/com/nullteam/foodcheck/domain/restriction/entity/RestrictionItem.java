package com.nullteam.foodcheck.domain.restriction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 개별 토글 항목(pork, meat …). */
@Entity
@Table(name = "restriction_items")
public class RestrictionItem {

	@Id
	private String code;

	@Column(nullable = false)
	private String name;

	protected RestrictionItem() {
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
