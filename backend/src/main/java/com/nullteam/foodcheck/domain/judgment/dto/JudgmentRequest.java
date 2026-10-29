package com.nullteam.foodcheck.domain.judgment.dto;

import java.util.List;

/**
 * POST /api/v1/judgments 요청 (REQ-3).
 *
 * @param userId    없거나 등록되지 않은 번호여도 404가 아니라 R0으로 판정한다
 * @param menuNames 1개 이상, 각 값은 비어 있지 않은 문자열이어야 한다
 */
public record JudgmentRequest(Long userId, List<String> menuNames) {
}
