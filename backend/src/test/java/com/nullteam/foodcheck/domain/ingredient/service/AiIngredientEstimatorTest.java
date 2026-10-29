package com.nullteam.foodcheck.domain.ingredient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

class AiIngredientEstimatorTest {

	@Test
	void 재료_이름을_다듬어_돌려준다() {
		ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
		when(builder.build().prompt().options(any()).user(anyString()).call().entity(AiIngredientEstimator.Estimated.class))
				.thenReturn(new AiIngredientEstimator.Estimated(List.of("된장", " 두부 ", "된장")));

		assertEquals(List.of("된장", "두부"), estimator(builder, Duration.ofSeconds(5)).estimate("된장찌개"));
	}

	@Test
	void AI가_늦으면_기다리지_않고_빈_목록을_돌려준다() {
		ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
		when(builder.build().prompt().options(any()).user(anyString()).call().entity(AiIngredientEstimator.Estimated.class))
				.thenAnswer(invocation -> {
					Thread.sleep(5_000);
					return new AiIngredientEstimator.Estimated(List.of("된장"));
				});

		long start = System.nanoTime();
		List<String> result = estimator(builder, Duration.ofMillis(200)).estimate("된장찌개");

		assertEquals(List.of(), result);
		assertTrue(Duration.ofNanos(System.nanoTime() - start).toMillis() < 2_000);
	}

	@SuppressWarnings("unchecked")
	private static AiIngredientEstimator estimator(ChatClient.Builder builder, Duration timeout) {
		ObjectProvider<ChatClient.Builder> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(builder);
		return new AiIngredientEstimator(provider, "openai/gpt-5-mini", timeout);
	}
}
