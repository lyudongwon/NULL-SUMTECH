package com.nullteam.foodcheck.domain.restaurant.dto;

/** POST /api/v1/restaurants/{restaurant_id}/feedback 요청. menuName·note는 선택이다. */
public record FeedbackRequest(String itemCode, String menuName, String note) {
}
