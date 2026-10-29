package com.nullteam.foodcheck.domain.restriction.service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 저장할 토글 상태를 계산한다(DB 없이 테스트하려고 분리).
 * 프리셋을 새로 고르면 모든 항목을 끄고 그 프리셋의 항목만 켠 뒤, 요청한 토글을 덮어쓴다.
 * 프리셋을 고르지 않았으면 지금 상태에 요청한 토글만 덮어쓴다.
 */
public final class ToggleMerger {

	private ToggleMerger() {
	}

	public static Map<String, Boolean> merge(
			Map<String, Boolean> current,
			Set<String> presetItems,
			Collection<String> allItemCodes,
			Map<String, Boolean> requested) {
		Map<String, Boolean> result = new TreeMap<>();
		if (presetItems != null) {
			for (String code : allItemCodes) {
				result.put(code, presetItems.contains(code));
			}
		} else {
			result.putAll(current);
		}
		result.putAll(requested);
		return result;
	}
}
