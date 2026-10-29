package com.nullteam.foodcheck.domain.auth.dto;

/** POST /api/v1/auth/login 요청. */
public record LoginRequest(String email, String password) {
}
