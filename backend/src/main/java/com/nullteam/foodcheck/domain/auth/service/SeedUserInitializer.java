package com.nullteam.foodcheck.domain.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * 앱이 시작할 때 시드 계정(SEED_USER_EMAIL·SEED_USER_PASSWORD)을 만든다. 채점기는 빈 DB에서 앱을 띄우고
 * docker-compose.yml에 적힌 이 계정을 E2E에 넘긴다. 둘 중 하나라도 비어 있으면 아무것도 하지 않는다.
 * 값이 가입 조건에 맞지 않아도 앱은 그대로 뜨고 경고만 남긴다(/__health가 막히지 않게).
 */
@Component
public class SeedUserInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(SeedUserInitializer.class);

	private final AuthService authService;
	private final String email;
	private final String password;

	public SeedUserInitializer(AuthService authService, @Value("${app.auth.seed-user.email:}") String email,
			@Value("${app.auth.seed-user.password:}") String password) {
		this.authService = authService;
		this.email = email;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		boolean hasEmail = email != null && !email.isBlank();
		boolean hasPassword = password != null && !password.isBlank();
		if (!hasEmail && !hasPassword) {
			return;
		}
		if (!hasEmail || !hasPassword) {
			log.warn("Seed user skipped: set both SEED_USER_EMAIL and SEED_USER_PASSWORD.");
			return;
		}
		try {
			boolean created = authService.seedAccount(email, password);
			log.info(created ? "Seed user created: {}" : "Seed user already exists: {}", AuthService.normalizeEmail(email));
		} catch (ApiException e) {
			log.warn("Seed user skipped: {}", e.getMessage());
		}
	}
}
