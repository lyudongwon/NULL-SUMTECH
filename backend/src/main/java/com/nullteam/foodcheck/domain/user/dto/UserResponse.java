package com.nullteam.foodcheck.domain.user.dto;

import java.time.OffsetDateTime;

public record UserResponse(Long userId, OffsetDateTime createdAt) {
}
