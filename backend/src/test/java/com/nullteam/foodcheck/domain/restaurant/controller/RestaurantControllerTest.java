package com.nullteam.foodcheck.domain.restaurant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nullteam.foodcheck.domain.restaurant.entity.Restaurant;
import com.nullteam.foodcheck.domain.restaurant.repository.RestaurantRepository;
import com.nullteam.foodcheck.domain.restaurant.service.RestaurantService;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;

/**
 * OPT-2 음식점 추천을 HTTP로 부른다. 추천 서비스는 진짜를 쓰고, 음식점 저장소와 사용자 제한(REQ-4)만 가짜로 둔다.
 * 응답 필드 이름·예외 상황은 docs/api.md > OPT-2와 같아야 한다.
 */
@WebMvcTest(RestaurantController.class)
@Import(RestaurantService.class)
class RestaurantControllerTest {

	private static final String URL = "/api/v1/restaurants/recommendations";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private RestaurantRepository restaurantRepository;

	@MockitoBean
	private RestrictionService restrictionService;

	@BeforeEach
	void 할랄_사용자가_있다() {
		when(restrictionService.findEnabledItemCodes(any())).thenReturn(Optional.empty());
		when(restrictionService.findEnabledItemCodes(7L)).thenReturn(Optional.of(Set.of("pork", "alcohol")));
	}

	@Test
	void 켠_제한을_모두_피하는_음식점만_snake_case로_돌려준다() throws Exception {
		List<Restaurant> restaurants = List.of(
				음식점(1, "할랄 식당", Set.of("pork", "alcohol", "peanut")),
				음식점(2, "분식집", Set.of("pork")));
		when(restaurantRepository.findAllByOrderByIdAsc()).thenReturn(restaurants);

		mvc.perform(get(URL).param("user_id", "7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.restaurants.length()").value(1))
				.andExpect(jsonPath("$.restaurants[0].restaurant_id").value(1))
				.andExpect(jsonPath("$.restaurants[0].name").value("할랄 식당"))
				.andExpect(jsonPath("$.restaurants[0].category").value("중동식"))
				.andExpect(jsonPath("$.restaurants[0].address").value("학교 앞"))
				// 사용자가 켠 값이 아니라 음식점이 피하는 항목 전부(peanut 포함)를 이름순으로 돌려준다
				.andExpect(jsonPath("$.restaurants[0].matched_restrictions.length()").value(3))
				.andExpect(jsonPath("$.restaurants[0].matched_restrictions[0]").value("alcohol"))
				.andExpect(jsonPath("$.restaurants[0].matched_restrictions[1]").value("peanut"))
				.andExpect(jsonPath("$.restaurants[0].matched_restrictions[2]").value("pork"));
	}

	@Test
	void limit이_없으면_5곳_범위_밖이면_1에서_20으로_맞춘다() throws Exception {
		List<Restaurant> restaurants = IntStream.rangeClosed(1, 25)
				.mapToObj(id -> 음식점(id, "식당" + id, Set.of("pork", "alcohol")))
				.toList();
		when(restaurantRepository.findAllByOrderByIdAsc()).thenReturn(restaurants);

		mvc.perform(get(URL).param("user_id", "7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.restaurants.length()").value(5));
		mvc.perform(get(URL).param("user_id", "7").param("limit", "0"))
				.andExpect(jsonPath("$.restaurants.length()").value(1));
		mvc.perform(get(URL).param("user_id", "7").param("limit", "100"))
				.andExpect(jsonPath("$.restaurants.length()").value(20));
	}

	@Test
	void 조건에_맞는_음식점이_없으면_200과_빈_목록이다() throws Exception {
		List<Restaurant> restaurants = List.of(음식점(2, "분식집", Set.of("pork")));
		when(restaurantRepository.findAllByOrderByIdAsc()).thenReturn(restaurants);

		mvc.perform(get(URL).param("user_id", "7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.restaurants.length()").value(0));
	}

	@Test
	void user_id가_없으면_400이다() throws Exception {
		mvc.perform(get(URL))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("user_id is required."));
		verifyNoInteractions(restaurantRepository);
	}

	@Test
	void 등록되지_않은_user_id는_404다() throws Exception {
		mvc.perform(get(URL).param("user_id", "999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("User not found."));
		verifyNoInteractions(restaurantRepository);
	}

	private static Restaurant 음식점(int id, String name, Set<String> satisfied) {
		Restaurant restaurant = mock(Restaurant.class);
		when(restaurant.getId()).thenReturn(id);
		when(restaurant.getName()).thenReturn(name);
		when(restaurant.getCategory()).thenReturn("중동식");
		when(restaurant.getAddress()).thenReturn("학교 앞");
		when(restaurant.getSatisfiedItems()).thenReturn(satisfied);
		return restaurant;
	}
}
