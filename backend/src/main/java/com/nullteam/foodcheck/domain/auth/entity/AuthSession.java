package com.nullteam.foodcheck.domain.auth.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** OPT-4 로그인 세션 하나(기기 하나). 쿠키 값은 저장하지 않고 SHA-256 해시만 저장한다. */
@Entity
@Table(name = "auth_sessions")
public class AuthSession {

	@Id
	@Column(name = "token_hash")
	private String tokenHash;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "expires_at", nullable = false)
	private OffsetDateTime expiresAt;

	protected AuthSession() {
	}

	public static AuthSession create(String tokenHash, Long userId, OffsetDateTime now, OffsetDateTime expiresAt) {
		AuthSession session = new AuthSession();
		session.tokenHash = tokenHash;
		session.userId = userId;
		session.createdAt = now;
		session.expiresAt = expiresAt;
		return session;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public Long getUserId() {
		return userId;
	}

	public OffsetDateTime getExpiresAt() {
		return expiresAt;
	}
}
