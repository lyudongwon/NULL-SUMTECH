package com.nullteam.foodcheck.domain.restaurant.dto;

/** POST /api/v1/restaurants/{restaurant_id}/feedback 응답. reportCount는 방금 것을 포함한 제보 사용자 수다. */
public record FeedbackResponse(Integer restaurantId, String itemCode, String menuName, String note, long reportCount) {
}
