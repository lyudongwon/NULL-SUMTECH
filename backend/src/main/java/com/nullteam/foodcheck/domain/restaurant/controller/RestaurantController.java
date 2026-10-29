package com.nullteam.foodcheck.domain.restaurant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.restaurant.dto.RecommendationResponse;
import com.nullteam.foodcheck.domain.restaurant.service.RestaurantService;

/** OPT-2 음식점 추천 (docs/api.md). */
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

	private final RestaurantService restaurantService;

	public RestaurantController(RestaurantService restaurantService) {
		this.restaurantService = restaurantService;
	}

	@GetMapping("/recommendations")
	public RecommendationResponse recommend(@RequestParam("user_id") Long userId,
			@RequestParam(value = "limit", required = false) Integer limit) {
		return restaurantService.recommend(userId, limit);
	}
}
