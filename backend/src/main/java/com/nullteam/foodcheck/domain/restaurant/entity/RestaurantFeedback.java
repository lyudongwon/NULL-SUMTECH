package com.nullteam.foodcheck.domain.restaurant.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** OPT-5 "이 가게에 이 항목을 쓰는 음식이 있다"는 사용자 제보. (가게, 사용자, 항목)마다 하나다. */
@Entity
@Table(name = "restaurant_feedback")
public class RestaurantFeedback {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "restaurant_id", nullable = false)
	private Integer restaurantId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "item_code", nullable = false)
	private String itemCode;

	@Column(name = "menu_name")
	private String menuName;

	private String note;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected RestaurantFeedback() {
	}

	public static RestaurantFeedback create(Integer restaurantId, Long userId, String itemCode) {
		RestaurantFeedback feedback = new RestaurantFeedback();
		feedback.restaurantId = restaurantId;
		feedback.userId = userId;
		feedback.itemCode = itemCode;
		feedback.createdAt = OffsetDateTime.now();
		return feedback;
	}

	public void update(String menuName, String note) {
		this.menuName = menuName;
		this.note = note;
	}

	public String getItemCode() {
		return itemCode;
	}

	public Integer getRestaurantId() {
		return restaurantId;
	}

	public String getMenuName() {
		return menuName;
	}

	public String getNote() {
		return note;
	}
}
