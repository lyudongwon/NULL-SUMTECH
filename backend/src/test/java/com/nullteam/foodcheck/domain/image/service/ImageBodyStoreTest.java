package com.nullteam.foodcheck.domain.image.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

/** 사진 본문은 메모리에 30분, 최대 200장만 둔다(docs/api.md > REQ-1 > 사진 본문 저장). */
class ImageBodyStoreTest {

	/** 테스트가 시간을 앞으로 돌릴 수 있는 시계. */
	static class MovableClock extends Clock {
		Instant now = Instant.parse("2026-10-30T00:00:00Z");

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}

	@Test
	void 저장한_본문과_형식을_그대로_돌려준다() {
		MovableClock clock = new MovableClock();
		ImageBodyStore store = new ImageBodyStore(clock);
		store.put("img_1", new byte[] { 1, 2, 3 }, "image/png");

		ImageBodyStore.Body body = store.find("img_1").orElseThrow();
		assertArrayEquals(new byte[] { 1, 2, 3 }, body.bytes());
		assertEquals("image/png", body.mimeType());
		assertEquals(clock.now, body.storedAt());
	}

	@Test
	void 없는_사진은_빈_값이다() {
		assertTrue(new ImageBodyStore(new MovableClock()).find("img_none").isEmpty());
	}

	@Test
	void 꼭_30분까지는_남고_그_뒤에는_지운다() {
		MovableClock clock = new MovableClock();
		ImageBodyStore store = new ImageBodyStore(clock);
		store.put("img_1", new byte[] { 1 }, "image/jpeg");

		clock.now = clock.now.plus(Duration.ofMinutes(30));
		assertTrue(store.find("img_1").isPresent());

		clock.now = clock.now.plusSeconds(1);
		assertTrue(store.find("img_1").isEmpty());

		// 만료로 지운 뒤에는 시간을 되돌려도 다시 나오지 않는다.
		clock.now = clock.now.minus(Duration.ofMinutes(31));
		assertTrue(store.find("img_1").isEmpty());
	}

	@Test
	void 이백장을_넘으면_가장_오래된_사진부터_지운다() {
		ImageBodyStore store = new ImageBodyStore(new MovableClock());
		assertEquals(200, ImageBodyStore.MAX_ENTRIES);
		for (int i = 0; i <= ImageBodyStore.MAX_ENTRIES; i++) {
			store.put("img_" + i, new byte[] { 1 }, "image/jpeg");
		}

		assertTrue(store.find("img_0").isEmpty());
		assertTrue(store.find("img_1").isPresent());
		assertTrue(store.find("img_" + ImageBodyStore.MAX_ENTRIES).isPresent());
	}
}
