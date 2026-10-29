package com.nullteam.foodcheck.domain.restriction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ToggleMergerTest {

	private static final List<String> ALL = List.of("alcohol", "meat", "peanut", "pork");

	@Test
	void 프리셋을_고르면_그_프리셋_항목만_켜고_요청한_토글을_덮어쓴다() {
		Map<String, Boolean> result = ToggleMerger.merge(
				Map.of("peanut", true),
				Set.of("pork", "alcohol"),
				ALL,
				Map.of("peanut", true));

		assertEquals(Map.of("alcohol", true, "meat", false, "peanut", true, "pork", true), result);
	}

	@Test
	void 프리셋을_고르지_않으면_지금_상태에_요청한_토글만_바꾼다() {
		Map<String, Boolean> result = ToggleMerger.merge(
				Map.of("pork", true, "alcohol", true),
				null,
				ALL,
				Map.of("meat", true, "alcohol", false));

		assertEquals(Map.of("pork", true, "alcohol", false, "meat", true), result);
	}

	@Test
	void 처음_저장에서_토글만_보내면_보낸_항목만_남는다() {
		Map<String, Boolean> result = ToggleMerger.merge(Map.of(), null, ALL, Map.of("meat", true));

		assertEquals(Map.of("meat", true), result);
	}
}
