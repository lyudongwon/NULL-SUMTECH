package com.nullteam.foodcheck.domain.auth.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.nullteam.foodcheck.global.exception.ApiException;

class SeedUserInitializerTest {

	private final AuthService authService = mock(AuthService.class);

	@Test
	void 이메일과_비밀번호가_둘_다_있으면_시드_계정을_만든다() {
		new SeedUserInitializer(authService, "seed@example.com", "seed-password").run(null);

		verify(authService).seedAccount("seed@example.com", "seed-password");
	}

	@Test
	void 둘_중_하나라도_비어_있으면_만들지_않는다() {
		new SeedUserInitializer(authService, "", "").run(null);
		new SeedUserInitializer(authService, "seed@example.com", " ").run(null);
		new SeedUserInitializer(authService, null, "seed-password").run(null);

		verify(authService, never()).seedAccount(anyString(), anyString());
	}

	@Test
	void 값이_가입_조건에_맞지_않아도_앱_시작을_막지_않는다() {
		when(authService.seedAccount(anyString(), anyString())).thenThrow(ApiException.badRequest("Please enter a valid email."));

		new SeedUserInitializer(authService, "not-an-email", "seed-password").run(null);
	}
}
