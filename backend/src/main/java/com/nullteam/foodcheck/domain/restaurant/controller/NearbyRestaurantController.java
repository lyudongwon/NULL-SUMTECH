package com.nullteam.foodcheck.domain.restaurant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.restaurant.dto.NearbyResponse;
import com.nullteam.foodcheck.domain.restaurant.dto.ReportRequest;
import com.nullteam.foodcheck.domain.restaurant.dto.ReportResponse;
import com.nullteam.foodcheck.domain.restaurant.service.NearbyRestaurantService;

/** OPT-2 주변 음식점 지도·음식점 제한 보고 (docs/api.md). */
@RestController
@RequestMapping("/api/v1/restaurants")
public class NearbyRestaurantController {

	private final NearbyRestaurantService nearbyRestaurantService;

	public NearbyRestaurantController(NearbyRestaurantService nearbyRestaurantService) {
		this.nearbyRestaurantService = nearbyRestaurantService;
	}

	@GetMapping("/nearby")
	public NearbyResponse nearby(@RequestParam("user_id") Long userId,
			@RequestParam(value = "lat", required = false) Double lat,
			@RequestParam(value = "lng", required = false) Double lng,
			@RequestParam(value = "radius", required = false) Integer radius) {
		return nearbyRestaurantService.nearby(userId, lat, lng, radius);
	}

	@PutMapping("/{restaurant_id}/reports")
	public ReportResponse report(@PathVariable("restaurant_id") Integer restaurantId,
			@RequestBody ReportRequest request) {
		return nearbyRestaurantService.report(restaurantId, request);
	}
}
