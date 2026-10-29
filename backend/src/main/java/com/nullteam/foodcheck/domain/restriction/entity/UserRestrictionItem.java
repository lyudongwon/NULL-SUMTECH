package com.nullteam.foodcheck.domain.restriction.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/** 토글 하나당 한 행. */
@Entity
@Table(name = "user_restriction_items")
@IdClass(UserRestrictionItem.Key.class)
public class UserRestrictionItem {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "item_code")
	private String itemCode;

	@Column(nullable = false)
	private boolean enabled;

	protected UserRestrictionItem() {
	}

	public String getItemCode() {
		return itemCode;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public static class Key implements Serializable {
		private Long userId;
		private String itemCode;

		public Key() {
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Key k && Objects.equals(userId, k.userId) && Objects.equals(itemCode, k.itemCode);
		}

		@Override
		public int hashCode() {
			return Objects.hash(userId, itemCode);
		}
	}
}
