package com.nullteam.foodcheck.domain.user.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 사용자를 구분하는 번호(user_id). 서버가 Auto Increment로 발급한다.
 * 가입(OPT-4)하면 email·passwordHash가 채워지고, 가입하지 않은 사용자는 둘 다 null이다.
 */
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected User() {
	}

	public static User create() {
		User user = new User();
		user.createdAt = OffsetDateTime.now();
		return user;
	}

	/** 이메일·비밀번호로 가입한 새 사용자. email은 이미 소문자·공백 제거한 값이다. */
	public static User register(String email, String passwordHash) {
		User user = create();
		user.email = email;
		user.passwordHash = passwordHash;
		return user;
	}

	public boolean isRegistered() {
		return email != null;
	}

	/** 가입하지 않고 쓰던 사용자에게 계정을 붙인다. 이미 가입한 사용자에게는 쓰지 않는다. */
	public void claim(String email, String passwordHash) {
		if (isRegistered()) {
			throw new IllegalStateException("User " + id + " is already registered.");
		}
		this.email = email;
		this.passwordHash = passwordHash;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
