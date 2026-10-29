package com.nullteam.foodcheck.domain.image.dto;

import java.time.OffsetDateTime;

/** POST /api/v1/images 응답. */
public record ImageUploadResponse(String imageId, int width, int height, OffsetDateTime uploadedAt) {
}
