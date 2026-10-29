package com.nullteam.foodcheck.domain.restriction.dto;

import java.time.OffsetDateTime;
import java.util.Map;

/** GET·PATCH /api/v1/users/{user_id}/restrictions 응답. */
public record RestrictionResponse(
		Long userId,
		String preset,
		Map<String, Boolean> toggles,
		int version,
		OffsetDateTime updatedAt) {
}
