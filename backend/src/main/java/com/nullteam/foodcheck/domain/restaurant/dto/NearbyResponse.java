package com.nullteam.foodcheck.domain.restaurant.dto;

import java.util.List;

/** GET /api/v1/restaurants/nearby 응답(docs/api.md > OPT-2 주변 음식점 지도). */
public record NearbyResponse(List<Item> restaurants) {

	/** pin: UNKNOWN(보고 없음)·SAFE(보고는 있고 내 항목에 걸리지 않음)·CAUTION(내 항목에 걸린다는 보고가 있음). */
	public record Item(Integer restaurantId, String name, String nameEn, String category, String address,
			double lat, double lng, Integer distanceM, String pin, List<String> reportedRestrictions,
			long reportCount) {
	}
}
