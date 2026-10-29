package com.nullteam.foodcheck.domain.restaurant.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.auth.service.AuthService;
import com.nullteam.foodcheck.domain.restaurant.dto.FeedbackRequest;
import com.nullteam.foodcheck.domain.restaurant.dto.FeedbackResponse;
import com.nullteam.foodcheck.domain.restaurant.dto.FeedbackSummaryResponse;
import com.nullteam.foodcheck.domain.restaurant.entity.RestaurantFeedback;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantFeedbackRepository;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantRepository;
import com.nullteam.foodcheck.domain.restriction.entity.RestrictionItem;
import com.nullteam.foodcheck.domain.restriction.repository.RestrictionItemRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

/** OPT-5 음식점 성분 제보. 로그인한 사용자가 (가게, 항목)마다 한 번 제보하고, 취소할 수 있다. */
@Service
public class RestaurantFeedbackService {

	static final int MAX_MENU_NAME = 100;
	static final int MAX_NOTE = 500;

	private final RestaurantFeedbackRepository feedbackRepository;
	private final RestaurantRepository restaurantRepository;
	private final RestrictionItemRepository itemRepository;
	private final AuthService authService;

	public RestaurantFeedbackService(RestaurantFeedbackRepository feedbackRepository,
			RestaurantRepository restaurantRepository, RestrictionItemRepository itemRepository,
			AuthService authService) {
		this.feedbackRepository = feedbackRepository;
		this.restaurantRepository = restaurantRepository;
		this.itemRepository = itemRepository;
		this.authService = authService;
	}

	@Transactional
	public FeedbackResponse submit(Integer restaurantId, String token, FeedbackRequest request) {
		Long userId = authService.requireUserId(token);
		requireRestaurant(restaurantId);
		String itemCode = request == null || request.itemCode() == null ? "" : request.itemCode().trim();
		if (itemCode.isEmpty() || !itemRepository.existsById(itemCode)) {
			throw ApiException.badRequest("Unknown ingredient.");
		}
		String menuName = clean(request.menuName(), MAX_MENU_NAME);
		String note = clean(request.note(), MAX_NOTE);

		RestaurantFeedback feedback = feedbackRepository
				.findByRestaurantIdAndUserIdAndItemCode(restaurantId, userId, itemCode)
				.orElseGet(() -> RestaurantFeedback.create(restaurantId, userId, itemCode));
		feedback.update(menuName, note);
		try {
			feedbackRepository.saveAndFlush(feedback);
		} catch (DataIntegrityViolationException e) {
			// 같은 사용자가 같은 제보를 동시에 보낸 경우: 먼저 들어간 것을 덮어쓴다
			feedback = feedbackRepository.findByRestaurantIdAndUserIdAndItemCode(restaurantId, userId, itemCode)
					.orElseThrow(() -> e);
			feedback.update(menuName, note);
		}
		long count = feedbackRepository.countByRestaurantIdAndItemCode(restaurantId, itemCode);
		return new FeedbackResponse(restaurantId, itemCode, feedback.getMenuName(), feedback.getNote(), count);
	}

	@Transactional(readOnly = true)
	public FeedbackSummaryResponse summary(Integer restaurantId, String token) {
		requireRestaurant(restaurantId);
		Optional<Long> me = authService.findUserId(token);
		Set<String> mine = me.map(id -> feedbackRepository.findByRestaurantIdAndUserId(restaurantId, id).stream()
				.map(RestaurantFeedback::getItemCode).collect(Collectors.toSet())).orElse(Set.of());
		var names = itemRepository.findAll().stream()
				.collect(Collectors.toMap(RestrictionItem::getCode, RestrictionItem::getName));

		List<FeedbackSummaryResponse.Item> items = feedbackRepository.countByRestaurant(restaurantId).stream()
				.map(c -> new FeedbackSummaryResponse.Item(c.getItemCode(), names.getOrDefault(c.getItemCode(), c.getItemCode()),
						c.getReports(), mine.contains(c.getItemCode())))
				.toList();
		return new FeedbackSummaryResponse(restaurantId, items);
	}

	@Transactional
	public void cancel(Integer restaurantId, String itemCode, String token) {
		Long userId = authService.requireUserId(token);
		feedbackRepository.deleteMine(restaurantId, userId, itemCode);
	}

	private void requireRestaurant(Integer restaurantId) {
		if (restaurantId == null || !restaurantRepository.existsById(restaurantId)) {
			throw ApiException.notFound("Restaurant not found.");
		}
	}

	/** 앞뒤 공백을 없애고 비면 null, 길면 잘라 저장한다. */
	static String clean(String value, int max) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
	}
}
