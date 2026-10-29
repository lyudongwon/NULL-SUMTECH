package com.nullteam.foodcheck.domain.restaurant.dto;

import java.util.Map;

/** PUT /api/v1/restaurants/{restaurant_id}/reports 응답. items에는 걸린다고 남은 항목(true)만 담는다. */
public record ReportResponse(Integer restaurantId, Map<String, Boolean> items, long reportCount) {
}
