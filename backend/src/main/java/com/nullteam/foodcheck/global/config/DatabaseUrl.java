package com.nullteam.foodcheck.global.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Render Postgres가 주는 DATABASE_URL(postgresql://user:pass@host:port/db)을 Spring datasource 설정으로 바꾼다.
 * JDBC 드라이버는 이 형식을 못 읽고, Render Blueprint는 host·port를 따로 넘겨 주지 않는다.
 * SPRING_DATASOURCE_URL이 있으면(docker compose·로컬) 그 값을 그대로 쓴다.
 */
public final class DatabaseUrl {

	private DatabaseUrl() {
	}

	/** 애플리케이션 시작 전에 부른다. 시스템 속성은 application.yml보다 우선한다. */
	public static void apply(Map<String, String> env) {
		String databaseUrl = env.get("DATABASE_URL");
		if (isBlank(databaseUrl) || !isBlank(env.get("SPRING_DATASOURCE_URL"))) {
			return;
		}
		toDatasourceProperties(databaseUrl).forEach(System::setProperty);
	}

	static Map<String, String> toDatasourceProperties(String databaseUrl) {
		URI uri = URI.create(databaseUrl);
		String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
		String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();

		Map<String, String> properties = new LinkedHashMap<>();
		properties.put("spring.datasource.url", "jdbc:postgresql://" + uri.getHost() + port + uri.getRawPath() + query);

		String userInfo = uri.getRawUserInfo();
		if (userInfo != null) {
			int colon = userInfo.indexOf(':');
			properties.put("spring.datasource.username", decode(colon < 0 ? userInfo : userInfo.substring(0, colon)));
			if (colon >= 0) {
				properties.put("spring.datasource.password", decode(userInfo.substring(colon + 1)));
			}
		}
		return properties;
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
