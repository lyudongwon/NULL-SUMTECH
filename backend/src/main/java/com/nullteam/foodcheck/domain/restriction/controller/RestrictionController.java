package com.nullteam.foodcheck.domain.restriction.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.restriction.dto.PresetListResponse;
import com.nullteam.foodcheck.domain.restriction.dto.RestrictionPatchRequest;
import com.nullteam.foodcheck.domain.restriction.dto.RestrictionResponse;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;

/** REQ-4 식단 제한 설정 (docs/api.md). */
@RestController
@RequestMapping("/api/v1")
public class RestrictionController {

	private final RestrictionService restrictionService;

	public RestrictionController(RestrictionService restrictionService) {
		this.restrictionService = restrictionService;
	}

	@GetMapping("/restrictions/presets")
	public PresetListResponse presets() {
		return restrictionService.listPresets();
	}

	@GetMapping("/users/{userId}/restrictions")
	public RestrictionResponse get(@PathVariable Long userId) {
		return restrictionService.get(userId);
	}

	@PatchMapping("/users/{userId}/restrictions")
	public RestrictionResponse patch(@PathVariable Long userId, @RequestBody(required = false) Map<String, Object> body) {
		return restrictionService.patch(userId, RestrictionPatchRequest.from(body));
	}
}
