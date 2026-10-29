package com.nullteam.foodcheck.domain.restaurant.service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.restaurant.dto.NearbyResponse;
import com.nullteam.foodcheck.domain.restaurant.dto.ReportRequest;
import com.nullteam.foodcheck.domain.restaurant.dto.ReportResponse;
import com.nullteam.foodcheck.domain.restaurant.entity.Restaurant;
import com.nullteam.foodcheck.domain.restaurant.entity.RestaurantReport;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantReportRepository;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantRepository;
import com.nullteam.foodcheck.domain.restriction.entity.RestrictionItem;
import com.nullteam.foodcheck.domain.restriction.repository.RestrictionItemRepository;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;
import com.nullteam.foodcheck.domain.user.service.UserService;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * OPT-2 주변 음식점 지도와 사용자 보고(docs/api.md > OPT-2). 음식점을 걸러내지 않고 모두 돌려주며,
 * 핑 상태는 저장하지 않고 사용자가 켠 제한 항목과 쌓인 보고를 맞대어 요청마다 계산한다(erd.md).
 * TODO(확인): 카카오맵 장소 검색은 아직 붙이지 않았다. 지금은 api.md의 "카카오맵 호출 실패" 경로처럼 DB에 쌓인 음식점만 돌려준다.
 */
@Service
public class NearbyRestaurantService {

	static final int MAX_RADIUS_M = 20000;
	private static final double EARTH_RADIUS_M = 6_371_000;

	private final RestaurantRepository restaurantRepository;
	private final RestaurantReportRepository reportRepository;
	private final RestrictionService restrictionService;
	private final RestrictionItemRepository itemRepository;
	private final UserService userService;
	private final Double defaultLat;
	private final Double defaultLng;
	private final int defaultRadiusM;

	public NearbyRestaurantService(RestaurantRepository restaurantRepository,
			RestaurantReportRepository reportRepository, RestrictionService restrictionService,
			RestrictionItemRepository itemRepository, UserService userService,
			@Value("${app.nearby.default-lat:}") String defaultLat,
			@Value("${app.nearby.default-lng:}") String defaultLng,
			@Value("${app.nearby.radius-m:1000}") int defaultRadiusM) {
		this.restaurantRepository = restaurantRepository;
		this.reportRepository = reportRepository;
		this.restrictionService = restrictionService;
		this.itemRepository = itemRepository;
		this.userService = userService;
		this.defaultLat = parse(defaultLat);
		this.defaultLng = parse(defaultLng);
		this.defaultRadiusM = defaultRadiusM;
	}

	@Transactional(readOnly = true)
	public NearbyResponse nearby(Long userId, Double lat, Double lng, Integer radius) {
		Set<String> enabled = restrictionService.findEnabledItemCodes(userId)
				.orElseThrow(() -> ApiException.notFound("User not found."));
		if ((lat == null) != (lng == null) || (lat != null && !validLocation(lat, lng))) {
			throw ApiException.badRequest("Location is not valid.");
		}
		Double centerLat = lat != null ? lat : defaultLat;
		Double centerLng = lng != null ? lng : defaultLng;
		boolean hasCenter = centerLat != null && centerLng != null;
		int radiusM = Math.clamp(radius == null ? defaultRadiusM : radius, 1, MAX_RADIUS_M);

		List<Restaurant> onMap = restaurantRepository.findAllByOrderByIdAsc().stream()
				.filter(r -> r.getLat() != null && r.getLng() != null)
				.toList();
		Map<Integer, List<RestaurantReport>> reports = reportRepository
				.findByIdRestaurantIdIn(onMap.stream().map(Restaurant::getId).toList()).stream()
				.collect(Collectors.groupingBy(r -> r.getId().getRestaurantId()));

		List<NearbyResponse.Item> items = onMap.stream()
				.map(r -> {
					Integer distance = hasCenter
							? (int) Math.round(distanceM(centerLat, centerLng, r.getLat(), r.getLng()))
							: null;
					return toItem(r, distance, reports.getOrDefault(r.getId(), List.of()), enabled);
				})
				// 기본 좌표가 정해지지 않았고 위치도 받지 못하면 거리를 잴 수 없어 반경으로 거르지 않고 번호 순으로 돌려준다
				.filter(item -> item.distanceM() == null || item.distanceM() <= radiusM)
				.sorted(Comparator.comparing(NearbyResponse.Item::distanceM,
						Comparator.nullsLast(Comparator.naturalOrder())))
				.toList();
		return new NearbyResponse(items);
	}

	@Transactional
	public ReportResponse report(Integer restaurantId, ReportRequest request) {
		Long userId = request == null ? null : request.userId();
		if (userId == null || !userService.exists(userId)) {
			throw ApiException.notFound("User not found.");
		}
		if (!restaurantRepository.existsById(restaurantId)) {
			throw ApiException.notFound("Restaurant not found.");
		}
		if (request.items() == null || request.items().isEmpty()) {
			throw ApiException.badRequest("Please choose at least one item.");
		}
		Set<String> known = itemRepository.findAll().stream()
				.map(RestrictionItem::getCode)
				.collect(Collectors.toSet());
		Set<String> reported = new TreeSet<>();
		for (Map.Entry<String, Boolean> entry : request.items().entrySet()) {
			if (!known.contains(entry.getKey())) {
				throw ApiException.badRequest("Unknown item: " + entry.getKey());
			}
			if (entry.getValue() == null) {
				throw ApiException.badRequest("Item values must be true or false.");
			}
			if (entry.getValue()) {
				reported.add(entry.getKey());
			}
		}

		// 사용자당 한 묶음: 다시 보내면 덮어쓰고, 모두 false면 그 사용자의 보고를 지운다
		RestaurantReport.Key key = new RestaurantReport.Key(restaurantId, userId);
		Optional<RestaurantReport> existing = reportRepository.findById(key);
		if (reported.isEmpty()) {
			existing.ifPresent(reportRepository::delete);
		} else {
			RestaurantReport report = existing.orElseGet(() -> new RestaurantReport(restaurantId, userId));
			report.replaceItems(reported, OffsetDateTime.now());
			reportRepository.save(report);
		}
		reportRepository.flush();

		Map<String, Boolean> items = new TreeMap<>();
		reported.forEach(code -> items.put(code, true));
		return new ReportResponse(restaurantId, items, reportRepository.countByIdRestaurantId(restaurantId));
	}

	private static NearbyResponse.Item toItem(Restaurant r, Integer distance, List<RestaurantReport> reports,
			Set<String> enabled) {
		Set<String> hits = reports.stream()
				.flatMap(report -> report.getItems().stream())
				.filter(enabled::contains)
				.collect(Collectors.toCollection(TreeSet::new));
		String pin = !hits.isEmpty() ? "CAUTION" : reports.isEmpty() ? "UNKNOWN" : "SAFE";
		return new NearbyResponse.Item(r.getId(), r.getName(), r.getNameEn(), r.getCategory(), r.getAddress(),
				r.getLat(), r.getLng(), distance, pin, List.copyOf(hits), reports.size());
	}

	private static boolean validLocation(double lat, double lng) {
		return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
	}

	/** 두 좌표 사이 직선(대원) 거리(m), 하버사인 공식. */
	static double distanceM(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a));
	}

	private static Double parse(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Double.valueOf(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
