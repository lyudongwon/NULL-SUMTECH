package com.nullteam.foodcheck.domain.restriction.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * PATCH /api/v1/users/{user_id}/restrictions 바디.
 * "preset" 키가 아예 없으면 프리셋을 바꾸지 않고, null이면 프리셋 선택을 지운다. 그래서 Map으로 받아 직접 읽는다.
 */
public record RestrictionPatchRequest(boolean presetGiven, String preset, Map<String, Boolean> toggles, Integer version) {

	public static RestrictionPatchRequest from(Map<String, Object> body) {
		if (body == null) {
			throw ApiException.badRequest("요청 형식이 올바르지 않습니다.");
		}
		boolean presetGiven = body.containsKey("preset");
		Object rawPreset = body.get("preset");
		if (rawPreset != null && !(rawPreset instanceof String)) {
			throw ApiException.badRequest("존재하지 않는 프리셋입니다.");
		}

		Map<String, Boolean> toggles = new LinkedHashMap<>();
		Object rawToggles = body.get("toggles");
		if (rawToggles != null) {
			if (!(rawToggles instanceof Map<?, ?> map)) {
				throw ApiException.badRequest("토글 형식이 올바르지 않습니다.");
			}
			for (Map.Entry<?, ?> e : map.entrySet()) {
				if (!(e.getValue() instanceof Boolean on)) {
					throw ApiException.badRequest("토글 값은 true 또는 false여야 합니다.");
				}
				toggles.put(String.valueOf(e.getKey()), on);
			}
		}

		Object rawVersion = body.get("version");
		if (rawVersion != null && !(rawVersion instanceof Number)) {
			throw ApiException.badRequest("version 형식이 올바르지 않습니다.");
		}
		Integer version = rawVersion == null ? null : ((Number) rawVersion).intValue();
		return new RestrictionPatchRequest(presetGiven, (String) rawPreset, toggles, version);
	}
}
