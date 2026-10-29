package com.nullteam.foodcheck.domain.auth.dto;

import java.time.OffsetDateTime;

/** 가입·로그인·내 계정 조회 응답. 비밀번호 관련 값은 절대 담지 않는다. */
public record AuthResponse(Long userId, String email, OffsetDateTime createdAt) {
}
