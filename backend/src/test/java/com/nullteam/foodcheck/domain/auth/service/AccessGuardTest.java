package com.nullteam.foodcheck.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.nullteam.foodcheck.domain.user.entity.User;
import com.nullteam.foodcheck.domain.user.repository.UserRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

class AccessGuardTest {

	private final UserRepository users = mock(UserRepository.class);
	private final AuthService auth = mock(AuthService.class);
	private final AccessGuard guard = new AccessGuard(users, auth);

	@Test
	void 가입하지_않은_번호와_없는_번호는_번호만으로_통과한다() {
		when(users.findById(1L)).thenReturn(Optional.of(User.create()));
		when(users.findById(2L)).thenReturn(Optional.empty());

		assertThatCode(() -> guard.requireAccess(1L, null)).doesNotThrowAnyException();
		assertThatCode(() -> guard.requireAccess(2L, null)).doesNotThrowAnyException();
	}

	@Test
	void 가입한_계정은_쿠키가_없으면_401() {
		when(users.findById(1L)).thenReturn(Optional.of(User.register("a@b.co", "hash")));
		when(auth.findUserId(null)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> guard.requireAccess(1L, null)).isInstanceOfSatisfying(ApiException.class,
				e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
	}

	@Test
	void 가입한_계정은_다른_사용자의_쿠키면_403() {
		when(users.findById(1L)).thenReturn(Optional.of(User.register("a@b.co", "hash")));
		when(auth.findUserId("t")).thenReturn(Optional.of(9L));

		assertThatThrownBy(() -> guard.requireAccess(1L, "t")).isInstanceOfSatisfying(ApiException.class, e -> {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(e.getMessage()).isEqualTo("This account belongs to someone else.");
		});
	}

	@Test
	void 가입한_계정은_자기_쿠키면_통과한다() {
		when(users.findById(1L)).thenReturn(Optional.of(User.register("a@b.co", "hash")));
		when(auth.findUserId("t")).thenReturn(Optional.of(1L));

		assertThatCode(() -> guard.requireAccess(1L, "t")).doesNotThrowAnyException();
	}
}
