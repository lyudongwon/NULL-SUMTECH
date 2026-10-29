package com.nullteam.foodcheck.domain.restaurant.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.restaurant.dto.RecommendationResponse;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantRepository;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * OPT-2 음식점 추천. 사용자가 켠 제한 항목을 모두 충족하는 음식점만 추천한다.
 * TODO(확인): "학식을 이용할 수 없는 경우"의 기준과 추천 순서가 정해지지 않아 id 순으로 돌려준다.
 */
@Service
public class RestaurantService {

	static final int DEFAULT_LIMIT = 5;
	static final int MAX_LIMIT = 20;

	private final RestaurantRepository restaurantRepository;
	private final RestrictionService restrictionService;

	public RestaurantService(RestaurantRepository restaurantRepository, RestrictionService restrictionService) {
		this.restaurantRepository = restaurantRepository;
		this.restrictionService = restrictionService;
	}

	@Transactional(readOnly = true)
	public RecommendationResponse recommend(Long userId, Integer limit) {
		Set<String> enabled = restrictionService.findEnabledItemCodes(userId)
				.orElseThrow(() -> ApiException.notFound("사용자 정보를 찾을 수 없습니다."));
		int size = limit == null ? DEFAULT_LIMIT : Math.clamp(limit, 1, MAX_LIMIT);

		List<RecommendationResponse.Item> items = restaurantRepository.findAllByOrderByIdAsc().stream()
				.filter(r -> r.getSatisfiedItems().containsAll(enabled))
				.limit(size)
				.map(r -> new RecommendationResponse.Item(r.getId(), r.getName(), r.getCategory(), r.getAddress(),
						List.copyOf(new TreeSet<>(enabled))))
				.toList();
		return new RecommendationResponse(items);
	}
}
