package com.nullteam.foodcheck.domain.restriction.dto;

import java.util.List;

/** GET /api/v1/restrictions/presets 응답. */
public record PresetListResponse(List<Preset> presets, List<Item> items) {

	public record Preset(String code, String name, List<String> items) {
	}

	public record Item(String code, String name) {
	}
}
