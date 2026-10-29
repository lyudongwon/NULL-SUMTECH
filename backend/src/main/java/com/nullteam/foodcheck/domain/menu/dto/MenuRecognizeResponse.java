package com.nullteam.foodcheck.domain.menu.dto;

import java.util.List;

/** POST /api/v1/menus/recognize 응답. 읽지 못한 메뉴는 추측하지 않고 name을 null로 둔다. */
public record MenuRecognizeResponse(String imageId, ImageType imageType, List<Menu> menus) {

	public record Menu(int index, String name) {
	}
}
