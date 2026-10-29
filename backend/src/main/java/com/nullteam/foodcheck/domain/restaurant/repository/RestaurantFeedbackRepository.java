package com.nullteam.foodcheck.domain.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.nullteam.foodcheck.domain.restaurant.entity.RestaurantFeedback;

public interface RestaurantFeedbackRepository extends JpaRepository<RestaurantFeedback, Long> {

	/** (가게, 항목)마다 제보한 사용자 수. 추천 필터링에 쓴다. */
	interface Count {
		Integer getRestaurantId();

		String getItemCode();

		long getReports();
	}

	@Query("select f.restaurantId as restaurantId, f.itemCode as itemCode, count(f) as reports "
			+ "from RestaurantFeedback f group by f.restaurantId, f.itemCode")
	List<Count> countAll();

	@Query("select f.restaurantId as restaurantId, f.itemCode as itemCode, count(f) as reports "
			+ "from RestaurantFeedback f where f.restaurantId = :restaurantId group by f.restaurantId, f.itemCode "
			+ "order by f.itemCode")
	List<Count> countByRestaurant(Integer restaurantId);

	Optional<RestaurantFeedback> findByRestaurantIdAndUserIdAndItemCode(Integer restaurantId, Long userId, String itemCode);

	List<RestaurantFeedback> findByRestaurantIdAndUserId(Integer restaurantId, Long userId);

	long countByRestaurantIdAndItemCode(Integer restaurantId, String itemCode);

	@Modifying
	@Query("delete from RestaurantFeedback f where f.restaurantId = :restaurantId and f.userId = :userId and f.itemCode = :itemCode")
	int deleteMine(Integer restaurantId, Long userId, String itemCode);
}
