package com.nullteam.foodcheck.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.user.repository.UserRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * 가입한 계정(OPT-4)의 데이터는 그 계정으로 로그인한 요청만 다룬다. 가입하지 않은 user_id는 지금처럼 번호만으로 쓴다.
 * 없는 번호는 여기서 막지 않는다(원래 404 처리를 각 서비스가 한다).
 */
@Service
public class AccessGuard {

	private final UserRepository userRepository;
	private final AuthService authService;

	public AccessGuard(UserRepository userRepository, AuthService authService) {
		this.userRepository = userRepository;
		this.authService = authService;
	}

	@Transactional(readOnly = true)
	public void requireAccess(Long userId, String token) {
		if (userId == null || !userRepository.findById(userId).map(u -> u.isRegistered()).orElse(false)) {
			return;
		}
		Long loggedIn = authService.findUserId(token).orElseThrow(() -> ApiException.unauthorized("Please log in."));
		if (!loggedIn.equals(userId)) {
			throw ApiException.forbidden("This account belongs to someone else.");
		}
	}
}
