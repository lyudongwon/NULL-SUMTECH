package com.nullteam.foodcheck.domain.auth.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.nullteam.foodcheck.domain.auth.entity.AuthSession;

public interface AuthSessionRepository extends JpaRepository<AuthSession, String> {

	Optional<AuthSession> findByTokenHashAndExpiresAtAfter(String tokenHash, OffsetDateTime now);

	@Modifying
	@Query("delete from AuthSession s where s.expiresAt < :now")
	int deleteExpired(OffsetDateTime now);
}
