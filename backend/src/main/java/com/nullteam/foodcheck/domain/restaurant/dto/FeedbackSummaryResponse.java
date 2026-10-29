package com.nullteam.foodcheck.domain.restaurant.dto;

import java.util.List;

/** GET /api/v1/restaurants/{restaurant_id}/feedback 응답. */
public record FeedbackSummaryResponse(Integer restaurantId, List<Item> items) {

	public record Item(String itemCode, String name, long reportCount, boolean reportedByMe) {
	}
}
