package com.nullteam.foodcheck.domain.image.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

/**
 * 사진 본문을 메모리에 잠깐 둔다. 판독(REQ-2)은 업로드 직후 한 번만 하므로 오래 둘 필요가 없다.
 * 서버를 다시 켜면 사라지고, 그 뒤 판독을 요청하면 404가 난다.
 * TODO(확인): ①이 images.data(bytea) 컬럼을 추가하면 DB에 저장하도록 바꾼다(docs/api.md > REQ-1 > 사진 본문 저장).
 */
@Component
public class ImageBodyStore {

	static final int MAX_ENTRIES = 200;
	static final Duration TTL = Duration.ofMinutes(30);

	public record Body(byte[] bytes, String mimeType, Instant storedAt) {
	}

	private final Clock clock;

	public ImageBodyStore() {
		this(Clock.systemUTC());
	}

	/** 테스트가 시간을 정해 30분 만료를 확인할 때 쓴다. */
	ImageBodyStore(Clock clock) {
		this.clock = clock;
	}

	/** 오래된 것부터 지운다(LinkedHashMap 삽입 순서). */
	private final Map<String, Body> bodies = new LinkedHashMap<>(16, 0.75f, false) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Body> eldest) {
			return size() > MAX_ENTRIES;
		}
	};

	public synchronized void put(String imageId, byte[] bytes, String mimeType) {
		bodies.put(imageId, new Body(bytes, mimeType, clock.instant()));
	}

	public synchronized Optional<Body> find(String imageId) {
		Body body = bodies.get(imageId);
		if (body == null) {
			return Optional.empty();
		}
		if (body.storedAt().plus(TTL).isBefore(clock.instant())) {
			bodies.remove(imageId);
			return Optional.empty();
		}
		return Optional.of(body);
	}
}
