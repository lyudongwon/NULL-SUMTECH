package com.nullteam.foodcheck.domain.image.service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * 사진 본문을 메모리에 잠깐 둔다. 판독(REQ-2)은 업로드 직후 한 번만 하므로 오래 둘 필요가 없다.
 * 서버를 다시 켜면 사라지고, 그 뒤 판독을 요청하면 404가 난다.
 * images.data(bytea) 컬럼이 생기면 DB에 저장하도록 바꿀 수 있다(docs/api.md > REQ-1 > 사진 본문 저장, docs/erd.md 열린 질문 1).
 */
@Component
public class ImageBodyStore {

	static final int MAX_ENTRIES = 200;
	static final Duration TTL = Duration.ofMinutes(30);

	public record Body(byte[] bytes, String mimeType, Instant storedAt) {
	}

	/** 오래된 것부터 지운다(LinkedHashMap 삽입 순서). */
	private final Map<String, Body> bodies = new LinkedHashMap<>(16, 0.75f, false) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Body> eldest) {
			return size() > MAX_ENTRIES;
		}
	};

	public synchronized void put(String imageId, byte[] bytes, String mimeType) {
		bodies.put(imageId, new Body(bytes, mimeType, Instant.now()));
	}

	public synchronized Optional<Body> find(String imageId) {
		Body body = bodies.get(imageId);
		if (body == null) {
			return Optional.empty();
		}
		if (body.storedAt().plus(TTL).isBefore(Instant.now())) {
			bodies.remove(imageId);
			return Optional.empty();
		}
		return Optional.of(body);
	}
}
