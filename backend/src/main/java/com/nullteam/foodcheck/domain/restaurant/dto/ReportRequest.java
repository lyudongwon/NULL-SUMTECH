package com.nullteam.foodcheck.domain.restaurant.dto;

import java.util.Map;

/** PUT /api/v1/restaurants/{restaurant_id}/reports 요청. items: 제한 항목 code → true(걸린다)·false(보고 취소). */
public record ReportRequest(Long userId, Map<String, Boolean> items) {
}
