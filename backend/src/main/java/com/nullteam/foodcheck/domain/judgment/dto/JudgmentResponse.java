package com.nullteam.foodcheck.domain.judgment.dto;

import java.util.List;

/** POST /api/v1/judgments 응답 (REQ-3). 요청한 menu_names와 같은 순서다. */
public record JudgmentResponse(List<JudgmentResult> results) {
}
