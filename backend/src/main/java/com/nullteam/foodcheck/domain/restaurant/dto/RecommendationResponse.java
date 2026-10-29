package com.nullteam.foodcheck.domain.restaurant.dto;

import java.util.List;

/** GET /api/v1/restaurants/recommendations 응답. */
public record RecommendationResponse(List<Item> restaurants) {

	public record Item(Integer restaurantId, String name, String category, String address,
			List<String> matchedRestrictions) {
	}
}
