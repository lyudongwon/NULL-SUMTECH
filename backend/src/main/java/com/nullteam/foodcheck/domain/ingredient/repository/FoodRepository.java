package com.nullteam.foodcheck.domain.ingredient.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nullteam.foodcheck.domain.ingredient.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Integer> {

	Optional<Food> findByName(String name);

	/**
	 * 띄어쓰기만 다른 이름도 같은 음식으로 본다. 판독한 이름("제육 볶음")과 시드 이름("제육볶음")을
	 * 맞추려는 것이다. TODO(확인): 동의어(돈까스/돈가스)까지 맞추는 방법은 docs/erd.md 열린 질문 5번이다.
	 */
	@Query(value = "SELECT * FROM foods WHERE replace(name, ' ', '') = :compactName LIMIT 1", nativeQuery = true)
	Optional<Food> findByCompactName(@Param("compactName") String compactName);
}
