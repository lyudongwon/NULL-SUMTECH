package com.nullteam.foodcheck.domain.restriction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nullteam.foodcheck.domain.restriction.entity.UserRestriction;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {

	/** 처음 저장. 같은 user_id가 동시에 들어와 이미 있으면 0을 돌려준다(→ 409). */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
			INSERT INTO user_restrictions (user_id, preset_code, version, updated_at)
			VALUES (:userId, :presetCode, 1, now())
			ON CONFLICT (user_id) DO NOTHING
			""", nativeQuery = true)
	int insertIfAbsent(@Param("userId") Long userId, @Param("presetCode") String presetCode);

	/** 조회한 version과 같을 때만 고친다. 다른 요청이 먼저 고쳤으면 0을 돌려준다(→ 409). */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
			UPDATE user_restrictions
			SET preset_code = :presetCode, version = version + 1, updated_at = now()
			WHERE user_id = :userId AND version = :version
			""", nativeQuery = true)
	int updateIfVersion(@Param("userId") Long userId, @Param("presetCode") String presetCode,
			@Param("version") int version);
}
