package com.nullteam.foodcheck.domain.menu.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.menu.dto.MenuRecognizeRequest;
import com.nullteam.foodcheck.domain.menu.dto.MenuRecognizeResponse;
import com.nullteam.foodcheck.domain.menu.service.MenuService;

/** REQ-2 메뉴 판독 (docs/api.md). */
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

	private final MenuService menuService;

	public MenuController(MenuService menuService) {
		this.menuService = menuService;
	}

	@PostMapping("/recognize")
	public MenuRecognizeResponse recognize(@RequestBody(required = false) MenuRecognizeRequest request) {
		return menuService.recognize(request);
	}
}
