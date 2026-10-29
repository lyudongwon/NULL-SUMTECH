package com.nullteam.foodcheck.domain.restaurant.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nullteam.foodcheck.domain.restaurant.entity.RestaurantReport;

public interface RestaurantReportRepository extends JpaRepository<RestaurantReport, RestaurantReport.Key> {

	List<RestaurantReport> findByIdRestaurantIdIn(Collection<Integer> restaurantIds);

	long countByIdRestaurantId(Integer restaurantId);
}
