package com.nullteam.foodcheck.domain.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nullteam.foodcheck.domain.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, String> {
}
