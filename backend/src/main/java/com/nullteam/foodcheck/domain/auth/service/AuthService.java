package com.nullteam.foodcheck.domain.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.auth.dto.AuthResponse;
import com.nullteam.foodcheck.domain.auth.dto.LoginRequest;
import com.nullteam.foodcheck.domain.auth.dto.SignupRequest;
import com.nullteam.foodcheck.domain.auth.entity.AuthSession;
import com.nullteam.foodcheck.domain.auth.repository.AuthSessionRepository;
import com.nullteam.foodcheck.domain.user.entity.User;
import com.nullteam.foodcheck.domain.user.repository.UserRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * OPT-4 회원가입·로그인·로그아웃. 이메일·비밀번호(bcrypt)로 가입하고, 로그인하면 서버에 세션을 두고
 * 무작위 토큰(쿠키 값)을 한 번만 돌려준다. docs/api.md > OPT-4.
 */
@Service
public class AuthService {

	public static final Duration SESSION_TTL = Duration.ofDays(14);
	static final int MIN_PASSWORD_LENGTH = 8;
	// 128자: bcrypt는 72바이트까지만 읽으니 그 이상을 받아도 뜻이 없고, 아주 긴 입력으로 서버를 괴롭히지 못하게 한다.
	static final int MAX_PASSWORD_LENGTH = 128;
	private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final AuthSessionRepository sessionRepository;
	private final PasswordEncoder passwordEncoder;
	// 없는 이메일로 로그인할 때도 해시 비교를 한 번 해서, 응답 시간으로 가입 여부를 알아내지 못하게 한다.
	private final String dummyHash;

	public AuthService(UserRepository userRepository, AuthSessionRepository sessionRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.sessionRepository = sessionRepository;
		this.passwordEncoder = passwordEncoder;
		this.dummyHash = passwordEncoder.encode("not-a-real-password");
	}

	/** 가입 결과와 새 세션 토큰. 토큰은 쿠키에만 담고 응답 본문에는 넣지 않는다. */
	public record Authenticated(AuthResponse user, String token) {
	}

	@Transactional
	public Authenticated signup(SignupRequest request) {
		String email = normalizeEmail(request == null ? null : request.email());
		String password = request.password();
		if (email == null || !EMAIL.matcher(email).matches()) {
			throw ApiException.badRequest("Please enter a valid email.");
		}
		if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
			throw ApiException.badRequest("Password must be at least 8 characters.");
		}
		if (userRepository.existsByEmail(email)) {
			throw ApiException.conflict("That email is already registered.");
		}

		String hash = passwordEncoder.encode(password);
		User user = claimable(request.userId());
		try {
			if (user != null) {
				user.claim(email, hash);
			} else {
				user = User.register(email, hash);
			}
			user = userRepository.saveAndFlush(user);
		} catch (DataIntegrityViolationException e) {
			// 같은 이메일로 동시에 가입한 경우
			throw ApiException.conflict("That email is already registered.");
		}
		return new Authenticated(toResponse(user), openSession(user.getId()));
	}

	@Transactional
	public Authenticated login(LoginRequest request) {
		String email = normalizeEmail(request == null ? null : request.email());
		String password = request == null ? null : request.password();
		if (email == null || password == null) {
			throw ApiException.badRequest("Email and password are required.");
		}
		Optional<User> found = userRepository.findByEmail(email);
		boolean ok = passwordEncoder.matches(password, found.map(User::getPasswordHash).orElse(dummyHash));
		if (found.isEmpty() || !ok) {
			throw ApiException.unauthorized("Incorrect email or password.");
		}
		User user = found.get();
		return new Authenticated(toResponse(user), openSession(user.getId()));
	}

	@Transactional
	public void logout(String token) {
		if (token == null || token.isBlank()) {
			return;
		}
		sessionRepository.deleteById(hash(token));
	}

	/** 쿠키 토큰으로 로그인한 사용자 번호를 찾는다. 없거나 만료됐으면 비어 있다. */
	@Transactional(readOnly = true)
	public Optional<Long> findUserId(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		return sessionRepository.findByTokenHashAndExpiresAtAfter(hash(token), OffsetDateTime.now())
				.map(AuthSession::getUserId);
	}

	/** 로그인이 필요한 요청에서 쓴다. 로그인하지 않았으면 401을 던진다. */
	@Transactional(readOnly = true)
	public Long requireUserId(String token) {
		return findUserId(token).orElseThrow(() -> ApiException.unauthorized("Please log in."));
	}

	@Transactional(readOnly = true)
	public AuthResponse me(String token) {
		Long userId = requireUserId(token);
		return userRepository.findById(userId).map(this::toResponse)
				.orElseThrow(() -> ApiException.unauthorized("Please log in."));
	}

	/** 만료된 세션 정리. 로그인할 때마다 하지 않고 호출하는 쪽이 가끔 부른다. */
	@Transactional
	public int deleteExpiredSessions() {
		return sessionRepository.deleteExpired(OffsetDateTime.now());
	}

	/** 가입 전에 쓰던 번호가 있고 아직 아무도 가입하지 않았다면 그 사용자를 돌려준다. 아니면 null. */
	private User claimable(Long userId) {
		if (userId == null) {
			return null;
		}
		return userRepository.findById(userId).filter(u -> !u.isRegistered()).orElse(null);
	}

	private String openSession(Long userId) {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		OffsetDateTime now = OffsetDateTime.now();
		sessionRepository.save(AuthSession.create(hash(token), userId, now, now.plus(SESSION_TTL)));
		return token;
	}

	private AuthResponse toResponse(User user) {
		return new AuthResponse(user.getId(), user.getEmail(), user.getCreatedAt());
	}

	static String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}

	static String hash(String token) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
