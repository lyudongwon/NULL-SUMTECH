package com.nullteam.foodcheck.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.user.dto.UserResponse;
import com.nullteam.foodcheck.domain.user.entity.User;
import com.nullteam.foodcheck.domain.user.repository.UserRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public UserResponse create() {
		User user = userRepository.save(User.create());
		return new UserResponse(user.getId(), user.getCreatedAt());
	}

	@Transactional(readOnly = true)
	public boolean exists(Long userId) {
		return userId != null && userRepository.existsById(userId);
	}

	/** 다른 도메인이 쓰는 확인용. 없는 사용자면 404를 던진다. */
	@Transactional(readOnly = true)
	public void requireExists(Long userId) {
		if (!exists(userId)) {
			throw ApiException.notFound("사용자 정보를 찾을 수 없습니다.");
		}
	}
}
