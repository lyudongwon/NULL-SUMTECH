package com.nullteam.foodcheck.domain.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class RestaurantServiceTest {

	@Test
	void 시드가_모두_충족하고_제보가_없으면_추천한다() {
		assertThat(RestaurantService.isSatisfied(Set.of("pork", "alcohol"), Set.of(), Set.of("pork"))).isTrue();
	}

	@Test
	void 시드가_충족하지_않으면_추천하지_않는다() {
		assertThat(RestaurantService.isSatisfied(Set.of("alcohol"), Set.of(), Set.of("pork"))).isFalse();
	}

	@Test
	void 내가_제한한_항목이_제보된_가게는_빠진다() {
		assertThat(RestaurantService.isSatisfied(Set.of("pork", "alcohol"), Set.of("pork"), Set.of("pork", "alcohol")))
				.isFalse();
	}

	@Test
	void 내가_제한하지_않은_항목의_제보는_영향이_없다() {
		assertThat(RestaurantService.isSatisfied(Set.of("pork", "peanut"), Set.of("peanut"), Set.of("pork"))).isTrue();
	}

	@Test
	void 제한한_항목이_없으면_제보가_있어도_추천한다() {
		assertThat(RestaurantService.isSatisfied(Set.of(), Set.of("pork"), Set.of())).isTrue();
	}
}
