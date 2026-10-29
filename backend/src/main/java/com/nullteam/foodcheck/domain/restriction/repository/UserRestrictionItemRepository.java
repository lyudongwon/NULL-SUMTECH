package com.nullteam.foodcheck.domain.restriction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nullteam.foodcheck.domain.restriction.entity.UserRestrictionItem;

public interface UserRestrictionItemRepository
		extends JpaRepository<UserRestrictionItem, UserRestrictionItem.Key> {

	List<UserRestrictionItem> findByUserIdOrderByItemCode(Long userId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(value = """
			INSERT INTO user_restriction_items (user_id, item_code, enabled)
			VALUES (:userId, :itemCode, :enabled)
			ON CONFLICT (user_id, item_code) DO UPDATE SET enabled = EXCLUDED.enabled
			""", nativeQuery = true)
	void upsert(@Param("userId") Long userId, @Param("itemCode") String itemCode,
			@Param("enabled") boolean enabled);
}
