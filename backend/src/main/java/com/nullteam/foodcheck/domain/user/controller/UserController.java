package com.nullteam.foodcheck.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.user.dto.UserResponse;
import com.nullteam.foodcheck.domain.user.service.UserService;

/** REQ-4 사용자 번호 발급. 회원가입·로그인 없이 user_id만 만든다. */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse create() {
		return userService.create();
	}
}
