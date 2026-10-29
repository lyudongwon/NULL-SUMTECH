package com.nullteam.foodcheck.domain.restaurant.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nullteam.foodcheck.domain.restaurant.entity.Restaurant;
import com.nullteam.foodcheck.domain.restaurant.entity.RestaurantReport;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantReportRepository;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantRepository;
import com.nullteam.foodcheck.domain.restaurant.service.NearbyRestaurantService;
import com.nullteam.foodcheck.domain.restriction.entity.RestrictionItem;
import com.nullteam.foodcheck.domain.restriction.repository.RestrictionItemRepository;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;
import com.nullteam.foodcheck.domain.user.service.UserService;

/**
 * OPT-2 주변 음식점 지도·제한 보고를 HTTP로 부른다. 서비스는 진짜, 저장소와 사용자 제한(REQ-4)만 가짜로 둔다.
 * 응답 필드 이름·핑 규칙·예외 상황은 docs/api.md > OPT-2와 같아야 한다. 기본 좌표(NEARBY_DEFAULT_*)는 비어 있다.
 */
@WebMvcTest(NearbyRestaurantController.class)
@Import(NearbyRestaurantService.class)
class NearbyRestaurantControllerTest {

	private static final String NEARBY = "/api/v1/restaurants/nearby";
	private static final double LAT = 35.14;
	private static final double LNG = 126.93;

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private RestaurantRepository restaurantRepository;

	@MockitoBean
	private RestaurantReportRepository reportRepository;

	@MockitoBean
	private RestrictionService restrictionService;

	@MockitoBean
	private RestrictionItemRepository itemRepository;

	@MockitoBean
	private UserService userService;

	@BeforeEach
	void 할랄_사용자가_있다() {
		when(restrictionService.findEnabledItemCodes(any())).thenReturn(Optional.empty());
		when(restrictionService.findEnabledItemCodes(7L)).thenReturn(Optional.of(Set.of("pork", "alcohol")));
		when(userService.exists(7L)).thenReturn(true);
		List<RestrictionItem> items = List.of(항목("pork"), 항목("alcohol"), 항목("peanut"));
		when(itemRepository.findAll()).thenReturn(items);
	}

	@Test
	void 걸러내지_않고_모두_돌려주며_보고로_핑을_정하고_가까운_순이다() throws Exception {
		// 위도 0.001도 ≈ 111m. 4번은 좌표가 없어 지도에 나오지 않는다
		List<Restaurant> restaurants = List.of(
				음식점(1, "돈가스집", LAT + 0.003, LNG),
				음식점(2, "케밥집", LAT + 0.001, LNG),
				음식점(3, "국숫집", LAT + 0.002, LNG),
				음식점(4, "좌표 없는 집", null, null));
		when(restaurantRepository.findAllByOrderByIdAsc()).thenReturn(restaurants);
		List<RestaurantReport> reports = List.of(
				보고(1, 8L, Set.of("pork", "peanut")), 보고(1, 9L, Set.of("peanut")),
				보고(3, 8L, Set.of("peanut")));
		when(reportRepository.findByIdRestaurantIdIn(any())).thenReturn(reports);

		mvc.perform(get(NEARBY).param("user_id", "7").param("lat", "" + LAT).param("lng", "" + LNG))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.restaurants.length()").value(3))
				// 보고가 없으면 미확인
				.andExpect(jsonPath("$.restaurants[0].restaurant_id").value(2))
				.andExpect(jsonPath("$.restaurants[0].name").value("케밥집"))
				.andExpect(jsonPath("$.restaurants[0].name_en").isEmpty())
				.andExpect(jsonPath("$.restaurants[0].category").value("한식"))
				.andExpect(jsonPath("$.restaurants[0].address").value("학교 앞"))
				.andExpect(jsonPath("$.restaurants[0].lat").value(LAT + 0.001))
				.andExpect(jsonPath("$.restaurants[0].lng").value(LNG))
				.andExpect(jsonPath("$.restaurants[0].distance_m").value(111))
				.andExpect(jsonPath("$.restaurants[0].pin").value("UNKNOWN"))
				.andExpect(jsonPath("$.restaurants[0].reported_restrictions.length()").value(0))
				.andExpect(jsonPath("$.restaurants[0].report_count").value(0))
				// 보고는 있지만 내 항목(pork·alcohol)에 걸리지 않으면 SAFE
				.andExpect(jsonPath("$.restaurants[1].restaurant_id").value(3))
				.andExpect(jsonPath("$.restaurants[1].distance_m").value(222))
				.andExpect(jsonPath("$.restaurants[1].pin").value("SAFE"))
				.andExpect(jsonPath("$.restaurants[1].reported_restrictions.length()").value(0))
				.andExpect(jsonPath("$.restaurants[1].report_count").value(1))
				// 내 항목에 걸린다는 보고가 1건이라도 있으면 CAUTION, 겹치는 항목만 담는다
				.andExpect(jsonPath("$.restaurants[2].restaurant_id").value(1))
				.andExpect(jsonPath("$.restaurants[2].distance_m").value(334))
				.andExpect(jsonPath("$.restaurants[2].pin").value("CAUTION"))
				.andExpect(jsonPath("$.restaurants[2].reported_restrictions.length()").value(1))
				.andExpect(jsonPath("$.restaurants[2].reported_restrictions[0]").value("pork"))
				.andExpect(jsonPath("$.restaurants[2].report_count").value(2));
	}

	@Test
	void 반경_밖은_빼고_반경은_최대_20000m다() throws Exception {
		List<Restaurant> restaurants = List.of(음식점(1, "가까운 집", LAT + 0.001, LNG), 음식점(2, "먼 집", LAT + 0.3, LNG));
		when(restaurantRepository.findAllByOrderByIdAsc()).thenReturn(restaurants);
		when(reportRepository.findByIdRestaurantIdIn(any())).thenReturn(List.of());

		// 반경을 안 보내면 기본 1000m
		mvc.perform(get(NEARBY).param("user_id", "7").param("lat", "" + LAT).param("lng", "" + LNG))
				.andExpect(jsonPath("$.restaurants.length()").value(1))
				.andExpect(jsonPath("$.restaurants[0].restaurant_id").value(1));
		// 약 33km 떨어진 곳은 반경을 크게 보내도 20000m에서 잘린다
		mvc.perform(get(NEARBY).param("user_id", "7").param("lat", "" + LAT).param("lng", "" + LNG)
				.param("radius", "50000"))
				.andExpect(jsonPath("$.restaurants.length()").value(1));
	}

	@Test
	void 주변에_음식점이_없으면_200과_빈_목록이다() throws Exception {
		when(restaurantRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

		mvc.perform(get(NEARBY).param("user_id", "7").param("lat", "" + LAT).param("lng", "" + LNG))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.restaurants.length()").value(0));
	}

	@Test
	void 위치도_기본_좌표도_없으면_거리_없이_번호_순으로_돌려준다() throws Exception {
		List<Restaurant> restaurants = List.of(음식점(1, "가", LAT + 0.3, LNG), 음식점(2, "나", LAT, LNG));
		when(restaurantRepository.findAllByOrderByIdAsc()).thenReturn(restaurants);
		when(reportRepository.findByIdRestaurantIdIn(any())).thenReturn(List.of());

		mvc.perform(get(NEARBY).param("user_id", "7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.restaurants.length()").value(2))
				.andExpect(jsonPath("$.restaurants[0].restaurant_id").value(1))
				.andExpect(jsonPath("$.restaurants[0].distance_m").isEmpty());
	}

	@Test
	void 좌표가_하나만_오거나_범위를_벗어나면_400이다() throws Exception {
		mvc.perform(get(NEARBY).param("user_id", "7").param("lat", "" + LAT))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Location is not valid."));
		mvc.perform(get(NEARBY).param("user_id", "7").param("lat", "91").param("lng", "" + LNG))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Location is not valid."));
		verifyNoInteractions(restaurantRepository);
	}

	@Test
	void user_id가_없으면_400_없는_사용자면_404다() throws Exception {
		mvc.perform(get(NEARBY))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("user_id is required."));
		mvc.perform(get(NEARBY).param("user_id", "999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("User not found."));
		verifyNoInteractions(restaurantRepository);
	}

	@Test
	void 보고는_걸린다는_항목만_저장하고_보고한_사람_수를_돌려준다() throws Exception {
		when(restaurantRepository.existsById(12)).thenReturn(true);
		when(reportRepository.findById(any())).thenReturn(Optional.empty());
		when(reportRepository.countByIdRestaurantId(12)).thenReturn(2L);

		mvc.perform(put("/api/v1/restaurants/12/reports").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"items\": {\"pork\": true, \"alcohol\": false}}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.restaurant_id").value(12))
				.andExpect(jsonPath("$.items.pork").value(true))
				.andExpect(jsonPath("$.items.alcohol").doesNotExist())
				.andExpect(jsonPath("$.report_count").value(2));

		ArgumentCaptor<RestaurantReport> saved = ArgumentCaptor.forClass(RestaurantReport.class);
		verify(reportRepository).save(saved.capture());
		assertEquals(new RestaurantReport.Key(12, 7L), saved.getValue().getId());
		assertEquals(Set.of("pork"), saved.getValue().getItems());
	}

	@Test
	void 다시_보내면_덮어쓰고_모두_false면_보고를_지운다() throws Exception {
		when(restaurantRepository.existsById(12)).thenReturn(true);
		RestaurantReport existing = new RestaurantReport(12, 7L);
		existing.replaceItems(Set.of("pork", "alcohol"), java.time.OffsetDateTime.now());
		when(reportRepository.findById(new RestaurantReport.Key(12, 7L))).thenReturn(Optional.of(existing));

		mvc.perform(put("/api/v1/restaurants/12/reports").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"items\": {\"alcohol\": true}}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.alcohol").value(true))
				.andExpect(jsonPath("$.items.pork").doesNotExist());
		assertEquals(Set.of("alcohol"), existing.getItems());

		mvc.perform(put("/api/v1/restaurants/12/reports").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"items\": {\"pork\": false, \"alcohol\": false}}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(0))
				.andExpect(jsonPath("$.report_count").value(0));
		verify(reportRepository).delete(existing);
	}

	@Test
	void 보고_예외는_api_md와_같다() throws Exception {
		when(restaurantRepository.existsById(12)).thenReturn(true);
		String url = "/api/v1/restaurants/12/reports";

		mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content("{\"items\": {\"pork\": true}}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("User not found."));
		mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 999, \"items\": {\"pork\": true}}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("User not found."));
		mvc.perform(put("/api/v1/restaurants/404/reports").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"items\": {\"pork\": true}}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Restaurant not found."));
		mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content("{\"user_id\": 7, \"items\": {}}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please choose at least one item."));
		mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content("{\"user_id\": 7}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please choose at least one item."));
		mvc.perform(put(url).contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"items\": {\"gluten\": true}}"))
				.andExpect(status().isBadRequest());
		verify(reportRepository, never()).save(any());
	}

	private static Restaurant 음식점(int id, String name, Double lat, Double lng) {
		Restaurant restaurant = mock(Restaurant.class);
		when(restaurant.getId()).thenReturn(id);
		when(restaurant.getName()).thenReturn(name);
		when(restaurant.getCategory()).thenReturn("한식");
		when(restaurant.getAddress()).thenReturn("학교 앞");
		when(restaurant.getLat()).thenReturn(lat);
		when(restaurant.getLng()).thenReturn(lng);
		return restaurant;
	}

	private static RestaurantReport 보고(int restaurantId, Long userId, Set<String> items) {
		RestaurantReport report = new RestaurantReport(restaurantId, userId);
		report.replaceItems(items, java.time.OffsetDateTime.now());
		return report;
	}

	private static RestrictionItem 항목(String code) {
		RestrictionItem item = mock(RestrictionItem.class);
		when(item.getCode()).thenReturn(code);
		return item;
	}
}
