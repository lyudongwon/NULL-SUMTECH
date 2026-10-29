package com.nullteam.foodcheck.domain.ingredient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/** REQ-5 1단계. 판독한 변형 메뉴 이름을 시드의 기본 요리로 잇는다(실험 A1·A2의 E1 흔들림). */
class MenuNameNormalizerTest {

	private static final List<String> SEEDS = List.of("설렁탕", "콩나물국", "김치찌개", "비빔밥", "비빔국수", "부대찌개", "떡볶이");

	@Test
	void 꾸밈말이_붙은_변형은_기본_요리로_잇는다() {
		assertEquals(Optional.of("비빔밥"), MenuNameNormalizer.baseDishOf("돌솥비빔밥", SEEDS));
		assertEquals(Optional.of("비빔국수"), MenuNameNormalizer.baseDishOf("김치비빔국수", SEEDS));
		assertEquals(Optional.of("김치찌개"), MenuNameNormalizer.baseDishOf("참치 김치찌개", SEEDS));
		assertEquals(Optional.of("떡볶이"), MenuNameNormalizer.baseDishOf("해물떡볶이", SEEDS));
	}

	@Test
	void 이름이_시드와_같으면_변형이_아니다() {
		assertEquals(Optional.empty(), MenuNameNormalizer.baseDishOf("비빔밥", SEEDS));
		assertEquals(Optional.empty(), MenuNameNormalizer.baseDishOf("비빔 밥", SEEDS));
	}

	@Test
	void 끝이_다르면_잇지_않는다() {
		assertEquals(Optional.empty(), MenuNameNormalizer.baseDishOf("콩나물국밥", SEEDS));
		assertEquals(Optional.empty(), MenuNameNormalizer.baseDishOf("비빔밥버거", SEEDS));
		assertEquals(Optional.empty(), MenuNameNormalizer.baseDishOf("된장찌개", SEEDS));
	}
}
