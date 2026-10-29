package com.nullteam.foodcheck.domain.restriction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nullteam.foodcheck.domain.restriction.entity.RestrictionItem;

public interface RestrictionItemRepository extends JpaRepository<RestrictionItem, String> {
}
