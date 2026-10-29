package com.nullteam.foodcheck.domain.user.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 회원 정보 없이 사용자를 구분하는 번호(user_id). 서버가 Auto Increment로 발급한다. */
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected User() {
	}

	public static User create() {
		User user = new User();
		user.createdAt = OffsetDateTime.now();
		return user;
	}

	public Long getId() {
		return id;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
