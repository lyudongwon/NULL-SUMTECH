package com.nullteam.foodcheck.domain.restaurant.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/** OPT-2 사용자 한 명이 한 음식점에 남긴 보고 한 묶음(erd.md restaurant_reports). 다시 보내면 덮어쓴다. */
@Entity
@Table(name = "restaurant_reports")
public class RestaurantReport {

	@EmbeddedId
	private Key id;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	/** "이 가게에서 걸린다"고 고른 제한 항목 코드(restaurant_report_items). */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "restaurant_report_items", joinColumns = {
			@JoinColumn(name = "restaurant_id", referencedColumnName = "restaurant_id"),
			@JoinColumn(name = "user_id", referencedColumnName = "user_id") })
	@Column(name = "item_code")
	private Set<String> items = new TreeSet<>();

	protected RestaurantReport() {
	}

	public RestaurantReport(Integer restaurantId, Long userId) {
		this.id = new Key(restaurantId, userId);
	}

	public Key getId() {
		return id;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public Set<String> getItems() {
		return items;
	}

	public void replaceItems(Set<String> codes, OffsetDateTime now) {
		items.clear();
		items.addAll(codes);
		updatedAt = now;
	}

	@Embeddable
	public static class Key implements Serializable {

		@Column(name = "restaurant_id")
		private Integer restaurantId;

		@Column(name = "user_id")
		private Long userId;

		protected Key() {
		}

		public Key(Integer restaurantId, Long userId) {
			this.restaurantId = restaurantId;
			this.userId = userId;
		}

		public Integer getRestaurantId() {
			return restaurantId;
		}

		public Long getUserId() {
			return userId;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Key k && Objects.equals(restaurantId, k.restaurantId) && Objects.equals(userId, k.userId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(restaurantId, userId);
		}
	}
}
