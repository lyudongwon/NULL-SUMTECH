package com.nullteam.foodcheck.domain.menu.dto;

/** POST /api/v1/menus/recognize 바디. */
public record MenuRecognizeRequest(String imageId) {
}
