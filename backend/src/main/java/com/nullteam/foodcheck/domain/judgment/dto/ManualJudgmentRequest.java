package com.nullteam.foodcheck.domain.judgment.dto;

/** POST /api/v1/judgments/manual 요청 (OPT-1). */
public record ManualJudgmentRequest(Long userId, String menuName) {
}
