package com.nullteam.foodcheck.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.nullteam.foodcheck.domain.auth.dto.LoginRequest;
import com.nullteam.foodcheck.domain.auth.dto.SignupRequest;
import com.nullteam.foodcheck.domain.auth.entity.AuthSession;
import com.nullteam.foodcheck.domain.auth.repository.AuthSessionRepository;
import com.nullteam.foodcheck.domain.user.entity.User;
import com.nullteam.foodcheck.domain.user.repository.UserRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

class AuthServiceTest {

	private final UserRepository users = mock(UserRepository.class);
	private final AuthSessionRepository sessions = mock(AuthSessionRepository.class);
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
	private AuthService service;

	@BeforeEach
	void setUp() {
		service = new AuthService(users, sessions, encoder);
		when(users.saveAndFlush(any(User.class))).thenAnswer(call -> {
			User user = call.getArgument(0);
			if (user.getId() == null) {
				ReflectionTestUtils.setField(user, "id", 7L);
			}
			return user;
		});
	}

	@Test
	void 가입하면_이메일을_소문자로_저장하고_비밀번호는_해시로_저장하며_세션을_연다() {
		var result = service.signup(new SignupRequest("  Student@Example.COM ", "password1", null));

		assertThat(result.user().email()).isEqualTo("student@example.com");
		assertThat(result.user().userId()).isEqualTo(7L);
		assertThat(result.token()).hasSizeGreaterThanOrEqualTo(40);
		verify(sessions).save(any(AuthSession.class));
	}

	@Test
	void 비밀번호는_평문으로_저장하지_않는다() {
		var saved = new User[1];
		when(users.saveAndFlush(any(User.class))).thenAnswer(call -> saved[0] = call.getArgument(0));

		service.signup(new SignupRequest("a@b.co", "password1", null));

		assertThat(saved[0].getPasswordHash()).isNotEqualTo("password1");
		assertThat(encoder.matches("password1", saved[0].getPasswordHash())).isTrue();
	}

	@Test
	void 가입하지_않은_user_id를_함께_보내면_그_번호가_계정이_된다() {
		User anonymous = User.create();
		ReflectionTestUtils.setField(anonymous, "id", 3L);
		when(users.findById(3L)).thenReturn(Optional.of(anonymous));

		var result = service.signup(new SignupRequest("a@b.co", "password1", 3L));

		assertThat(result.user().userId()).isEqualTo(3L);
		assertThat(anonymous.isRegistered()).isTrue();
	}

	@Test
	void 이미_가입한_user_id는_가져가지_못하고_새_번호를_받는다() {
		User taken = User.register("owner@b.co", "hash");
		ReflectionTestUtils.setField(taken, "id", 3L);
		when(users.findById(3L)).thenReturn(Optional.of(taken));

		var result = service.signup(new SignupRequest("a@b.co", "password1", 3L));

		assertThat(result.user().userId()).isEqualTo(7L);
		assertThat(taken.getEmail()).isEqualTo("owner@b.co");
	}

	@Test
	void 잘못된_이메일은_400() {
		assertThatThrownBy(() -> service.signup(new SignupRequest("not-an-email", "password1", null)))
				.isInstanceOfSatisfying(ApiException.class, e -> {
					assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
					assertThat(e.getMessage()).isEqualTo("Please enter a valid email.");
				});
		verify(users, never()).saveAndFlush(any());
	}

	@Test
	void 짧은_비밀번호는_400() {
		assertThatThrownBy(() -> service.signup(new SignupRequest("a@b.co", "short", null)))
				.isInstanceOfSatisfying(ApiException.class, e -> {
					assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
					assertThat(e.getMessage()).isEqualTo("Password must be at least 8 characters.");
				});
	}

	@Test
	void 이미_가입한_이메일은_409() {
		when(users.existsByEmail("a@b.co")).thenReturn(true);

		assertThatThrownBy(() -> service.signup(new SignupRequest("A@b.co", "password1", null)))
				.isInstanceOfSatisfying(ApiException.class, e -> {
					assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT);
					assertThat(e.getMessage()).isEqualTo("That email is already registered.");
				});
	}

	@Test
	void 동시에_같은_이메일로_가입하면_409() {
		when(users.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("dup"));

		assertThatThrownBy(() -> service.signup(new SignupRequest("a@b.co", "password1", null)))
				.isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT));
	}

	@Test
	void 맞는_비밀번호로_로그인하면_세션을_연다() {
		when(users.findByEmail("a@b.co")).thenReturn(Optional.of(User.register("a@b.co", encoder.encode("password1"))));

		var result = service.login(new LoginRequest(" A@b.co", "password1"));

		assertThat(result.user().email()).isEqualTo("a@b.co");
		verify(sessions).save(any(AuthSession.class));
	}

	@Test
	void 틀린_비밀번호와_없는_이메일은_같은_401_문장이다() {
		when(users.findByEmail("a@b.co")).thenReturn(Optional.of(User.register("a@b.co", encoder.encode("password1"))));
		when(users.findByEmail("nobody@b.co")).thenReturn(Optional.empty());

		for (var request : new LoginRequest[] { new LoginRequest("a@b.co", "wrong-password"),
				new LoginRequest("nobody@b.co", "password1") }) {
			assertThatThrownBy(() -> service.login(request)).isInstanceOfSatisfying(ApiException.class, e -> {
				assertThat(e.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
				assertThat(e.getMessage()).isEqualTo("Incorrect email or password.");
			});
		}
		verify(sessions, never()).save(any());
	}

	@Test
	void 로그아웃은_세션을_지우고_쿠키가_없어도_오류가_없다() {
		service.logout("some-token");
		service.logout(null);
		service.logout("  ");

		verify(sessions).deleteById(AuthService.hash("some-token"));
	}

	@Test
	void 만료됐거나_없는_토큰은_로그인하지_않은_것이다() {
		when(sessions.findByTokenHashAndExpiresAtAfter(anyString(), any())).thenReturn(Optional.empty());

		assertThat(service.findUserId("token")).isEmpty();
		assertThat(service.findUserId(null)).isEmpty();
		assertThatThrownBy(() -> service.requireUserId("token")).isInstanceOfSatisfying(ApiException.class, e -> {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(e.getMessage()).isEqualTo("Please log in.");
		});
	}

	@Test
	void 시드_계정은_없을_때만_해시한_비밀번호로_만들고_세션은_열지_않는다() {
		var saved = new User[1];
		when(users.saveAndFlush(any(User.class))).thenAnswer(call -> saved[0] = call.getArgument(0));

		assertThat(service.seedAccount(" Seed@Example.com ", "seed-password")).isTrue();

		assertThat(saved[0].getEmail()).isEqualTo("seed@example.com");
		assertThat(encoder.matches("seed-password", saved[0].getPasswordHash())).isTrue();
		verify(sessions, never()).save(any());
	}

	@Test
	void 시드_계정이_이미_있으면_그대로_둔다() {
		when(users.existsByEmail("seed@example.com")).thenReturn(true);

		assertThat(service.seedAccount("seed@example.com", "seed-password")).isFalse();
		verify(users, never()).saveAndFlush(any());
	}

	@Test
	void 시드_계정도_가입과_같은_이메일_비밀번호_조건을_따른다() {
		assertThatThrownBy(() -> service.seedAccount("not-an-email", "seed-password")).isInstanceOf(ApiException.class);
		assertThatThrownBy(() -> service.seedAccount("seed@example.com", "short")).isInstanceOf(ApiException.class);
		verify(users, never()).saveAndFlush(any());
	}
}
