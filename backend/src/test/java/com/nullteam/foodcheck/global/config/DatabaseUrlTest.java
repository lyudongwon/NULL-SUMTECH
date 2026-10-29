package com.nullteam.foodcheck.global.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DatabaseUrlTest {

	@Test
	void Render_내부_주소는_포트_없이_JDBC_주소와_계정으로_바꾼다() {
		Map<String, String> result = DatabaseUrl.toDatasourceProperties(
				"postgresql://foodcheck:s3cret@dpg-abc123-a/foodcheck");

		assertEquals(Map.of(
				"spring.datasource.url", "jdbc:postgresql://dpg-abc123-a/foodcheck",
				"spring.datasource.username", "foodcheck",
				"spring.datasource.password", "s3cret"), result);
	}

	@Test
	void 포트와_쿼리를_유지하고_인코딩된_비밀번호를_푼다() {
		Map<String, String> result = DatabaseUrl.toDatasourceProperties(
				"postgresql://foodcheck:p%40ss%3Aword@db.example.com:5433/foodcheck?sslmode=require");

		assertEquals("jdbc:postgresql://db.example.com:5433/foodcheck?sslmode=require",
				result.get("spring.datasource.url"));
		assertEquals("p@ss:word", result.get("spring.datasource.password"));
	}
}
