package com.nullteam.foodcheck.domain.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nullteam.foodcheck.domain.auth.dto.AuthResponse;
import com.nullteam.foodcheck.domain.auth.dto.LoginRequest;
import com.nullteam.foodcheck.domain.auth.dto.SignupRequest;
import com.nullteam.foodcheck.domain.auth.service.AuthService;

/** OPT-4 회원가입·로그인·로그아웃·내 계정 (docs/api.md). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	public static final String COOKIE = "session";

	private final AuthService authService;
	private final boolean cookieSecure;

	public AuthController(AuthService authService, @Value("${app.auth.cookie-secure:false}") boolean cookieSecure) {
		this.authService = authService;
		this.cookieSecure = cookieSecure;
	}

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@RequestBody(required = false) SignupRequest request) {
		if (request == null) {
			request = new SignupRequest(null, null, null);
		}
		AuthService.Authenticated result = authService.signup(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, sessionCookie(result.token()).toString())
				.body(result.user());
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody(required = false) LoginRequest request) {
		AuthService.Authenticated result = authService.login(request);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, sessionCookie(result.token()).toString())
				.body(result.user());
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = COOKIE, required = false) String token) {
		authService.logout(token);
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, base("").maxAge(0).build().toString())
				.build();
	}

	@GetMapping("/me")
	public AuthResponse me(@CookieValue(name = COOKIE, required = false) String token) {
		return authService.me(token);
	}

	private ResponseCookie sessionCookie(String token) {
		return base(token).maxAge(AuthService.SESSION_TTL).build();
	}

	private ResponseCookie.ResponseCookieBuilder base(String value) {
		return ResponseCookie.from(COOKIE, value).httpOnly(true).secure(cookieSecure).sameSite("Lax").path("/");
	}
}
