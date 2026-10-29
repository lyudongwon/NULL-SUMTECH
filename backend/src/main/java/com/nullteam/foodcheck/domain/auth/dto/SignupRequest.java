package com.nullteam.foodcheck.domain.auth.dto;

/** POST /api/v1/auth/signup 요청. userId는 가입 전에 쓰던 번호(선택)다. */
public record SignupRequest(String email, String password, Long userId) {
}
