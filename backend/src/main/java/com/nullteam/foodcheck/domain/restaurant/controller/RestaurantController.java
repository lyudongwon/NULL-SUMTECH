package com.nullteam.foodcheck.domain.restaurant.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.auth.controller.AuthController;
import com.nullteam.foodcheck.domain.auth.service.AccessGuard;
import com.nullteam.foodcheck.domain.restaurant.dto.FeedbackRequest;
import com.nullteam.foodcheck.domain.restaurant.dto.FeedbackResponse;
import com.nullteam.foodcheck.domain.restaurant.dto.FeedbackSummaryResponse;
import com.nullteam.foodcheck.domain.restaurant.dto.RecommendationResponse;
import com.nullteam.foodcheck.domain.restaurant.service.RestaurantFeedbackService;
import com.nullteam.foodcheck.domain.restaurant.service.RestaurantService;

/** OPT-2 음식점 추천, OPT-5 음식점 성분 제보 (docs/api.md). */
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

	private final RestaurantService restaurantService;
	private final RestaurantFeedbackService feedbackService;
	private final AccessGuard accessGuard;

	public RestaurantController(RestaurantService restaurantService, RestaurantFeedbackService feedbackService,
			AccessGuard accessGuard) {
		this.restaurantService = restaurantService;
		this.feedbackService = feedbackService;
		this.accessGuard = accessGuard;
	}

	@GetMapping("/recommendations")
	public RecommendationResponse recommend(@RequestParam("user_id") Long userId,
			@RequestParam(value = "limit", required = false) Integer limit,
			@CookieValue(name = AuthController.COOKIE, required = false) String token) {
		accessGuard.requireAccess(userId, token);
		return restaurantService.recommend(userId, limit);
	}

	@PostMapping("/{restaurantId}/feedback")
	@ResponseStatus(HttpStatus.CREATED)
	public FeedbackResponse submitFeedback(@PathVariable Integer restaurantId,
			@RequestBody(required = false) FeedbackRequest request,
			@CookieValue(name = AuthController.COOKIE, required = false) String token) {
		return feedbackService.submit(restaurantId, token, request);
	}

	@GetMapping("/{restaurantId}/feedback")
	public FeedbackSummaryResponse feedback(@PathVariable Integer restaurantId,
			@CookieValue(name = AuthController.COOKIE, required = false) String token) {
		return feedbackService.summary(restaurantId, token);
	}

	@DeleteMapping("/{restaurantId}/feedback/{itemCode}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelFeedback(@PathVariable Integer restaurantId, @PathVariable String itemCode,
			@CookieValue(name = AuthController.COOKIE, required = false) String token) {
		feedbackService.cancel(restaurantId, itemCode, token);
	}
}
