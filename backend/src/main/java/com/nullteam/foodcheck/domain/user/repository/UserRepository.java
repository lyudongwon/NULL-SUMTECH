package com.nullteam.foodcheck.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nullteam.foodcheck.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
