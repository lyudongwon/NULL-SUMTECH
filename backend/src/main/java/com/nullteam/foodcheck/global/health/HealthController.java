package com.nullteam.foodcheck.global.health;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 백엔드 상태 확인. 화면(Next.js)의 /__health와 별개로 컨테이너 healthcheck가 쓴다. */
@RestController
public class HealthController {

	private final String releaseId;

	public HealthController(@Value("${app.release-id:}") String releaseId) {
		this.releaseId = releaseId;
	}

	@GetMapping("/__health")
	public Map<String, Object> health() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", "ok");
		body.put("release_id", releaseId.isBlank() ? null : releaseId);
		return body;
	}
}
