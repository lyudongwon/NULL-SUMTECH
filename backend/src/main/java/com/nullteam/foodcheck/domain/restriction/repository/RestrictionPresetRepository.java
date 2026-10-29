package com.nullteam.foodcheck.domain.restriction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nullteam.foodcheck.domain.restriction.entity.RestrictionPreset;

public interface RestrictionPresetRepository extends JpaRepository<RestrictionPreset, String> {
}
