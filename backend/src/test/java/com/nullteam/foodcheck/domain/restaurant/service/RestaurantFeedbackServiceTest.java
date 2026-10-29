package com.nullteam.foodcheck.domain.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RestaurantFeedbackServiceTest {

	@Test
	void 빈_값은_null_앞뒤_공백은_제거() {
		assertThat(RestaurantFeedbackService.clean(null, 10)).isNull();
		assertThat(RestaurantFeedbackService.clean("   ", 10)).isNull();
		assertThat(RestaurantFeedbackService.clean("  김치찌개 ", 10)).isEqualTo("김치찌개");
	}

	@Test
	void 길면_최대_길이로_자른다() {
		assertThat(RestaurantFeedbackService.clean("a".repeat(150), 100)).hasSize(100);
	}
}
