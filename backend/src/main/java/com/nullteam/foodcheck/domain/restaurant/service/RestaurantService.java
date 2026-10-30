package com.nullteam.foodcheck.domain.restaurant.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.restaurant.dto.RecommendationResponse;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantFeedbackRepository;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantRepository;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * OPT-2 음식점 추천. 사용자가 켠 제한 항목을 모두 충족하는 음식점만 추천한다.
 * OPT-5: 사용자 제보가 HIDE_THRESHOLD명 이상인 (가게, 항목)은 시드가 충족한다고 해도 충족하지 않는 것으로 본다.
 * TODO(확인): "학식을 이용할 수 없는 경우"의 기준과 추천 순서가 정해지지 않아 id 순으로 돌려준다.
 */
@Service
public class RestaurantService {

	static final int DEFAULT_LIMIT = 5;
	static final int MAX_LIMIT = 20;
	/** 한 항목이 이 수 이상의 사용자에게 제보되면 그 가게는 그 항목을 제한한 사람의 추천에서 빠진다. */
	static final int HIDE_THRESHOLD = 1;

	private final RestaurantRepository restaurantRepository;
	private final RestrictionService restrictionService;
	private final RestaurantFeedbackRepository feedbackRepository;

	public RestaurantService(RestaurantRepository restaurantRepository, RestrictionService restrictionService,
			RestaurantFeedbackRepository feedbackRepository) {
		this.restaurantRepository = restaurantRepository;
		this.restrictionService = restrictionService;
		this.feedbackRepository = feedbackRepository;
	}

	@Transactional(readOnly = true)
	public RecommendationResponse recommend(Long userId, Integer limit) {
		Set<String> enabled = restrictionService.findEnabledItemCodes(userId)
				.orElseThrow(() -> ApiException.notFound("User not found."));
		int size = limit == null ? DEFAULT_LIMIT : Math.clamp(limit, 1, MAX_LIMIT);

		Map<Integer, Set<String>> reported = reportedItems();
		List<RecommendationResponse.Item> items = restaurantRepository.findAllByOrderByIdAsc().stream()
				.filter(r -> isSatisfied(r.getSatisfiedItems(), reported.getOrDefault(r.getId(), Set.of()), enabled))
				.limit(size)
				.map(r -> new RecommendationResponse.Item(r.getId(), r.getName(), r.getCategory(), r.getAddress(),
						List.copyOf(new TreeSet<>(enabled))))
				.toList();
		return new RecommendationResponse(items);
	}

	/** 가게가 켠 항목을 모두 충족하는가. 제보가 문턱을 넘은 항목은 충족하지 않는다. */
	static boolean isSatisfied(Set<String> satisfied, Set<String> reported, Set<String> enabled) {
		return satisfied.containsAll(enabled) && enabled.stream().noneMatch(reported::contains);
	}

	/** 가게마다 제보가 문턱을 넘은 항목들. */
	private Map<Integer, Set<String>> reportedItems() {
		Map<Integer, Set<String>> reported = new HashMap<>();
		for (RestaurantFeedbackRepository.Count count : feedbackRepository.countAll()) {
			if (count.getReports() >= HIDE_THRESHOLD) {
				reported.computeIfAbsent(count.getRestaurantId(), id -> new HashSet<>()).add(count.getItemCode());
			}
		}
		return reported;
	}
}
