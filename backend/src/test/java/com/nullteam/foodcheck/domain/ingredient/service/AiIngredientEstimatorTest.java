package com.nullteam.foodcheck.domain.ingredient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

/**
 * REQ-5 4단계 AI 추정. AI를 못 부르면(키 미설정·호출 실패·빈 응답) 빈 목록을 돌려줘 판정이 멈추지 않고
 * R3("확인 필요")로 가는지, 받은 재료 이름을 정리·중복 제거·최대 12개로 자르는지 본다.
 */
class AiIngredientEstimatorTest {

	@SuppressWarnings("unchecked")
	private final ObjectProvider<ChatClient.Builder> provider = mock(ObjectProvider.class);

	private final ChatClient.Builder builder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);

	private final AiIngredientEstimator estimator = new AiIngredientEstimator(provider);

	private void aiAnswers(AiIngredientEstimator.Estimated estimated) {
		when(provider.getIfAvailable()).thenReturn(builder);
		when(builder.build().prompt().user(anyString()).call().entity(AiIngredientEstimator.Estimated.class))
				.thenReturn(estimated);
	}

	@Test
	void AI_모델이_설정되지_않으면_빈_목록이다() {
		when(provider.getIfAvailable()).thenReturn(null);

		assertEquals(List.of(), estimator.estimate("두부조림"));
	}

	@Test
	void AI_호출이_실패하면_빈_목록이다() {
		when(provider.getIfAvailable()).thenReturn(builder);
		when(builder.build().prompt().user(anyString()).call().entity(AiIngredientEstimator.Estimated.class))
				.thenThrow(new RuntimeException("timeout"));

		assertEquals(List.of(), estimator.estimate("두부조림"));
	}

	@Test
	void 응답이_비었거나_재료가_null이면_빈_목록이다() {
		aiAnswers(null);
		assertEquals(List.of(), estimator.estimate("두부조림"));

		aiAnswers(new AiIngredientEstimator.Estimated(null));
		assertEquals(List.of(), estimator.estimate("두부조림"));
	}

	@Test
	void 재료_이름은_정리하고_빈_값과_중복을_빼고_순서를_지킨다() {
		aiAnswers(new AiIngredientEstimator.Estimated(Arrays.asList(" 두부 ", null, "", "간장", "두부", "맛술")));

		assertEquals(List.of("두부", "간장", "맛술"), estimator.estimate("두부조림"));
	}

	@Test
	void 재료는_12개까지만_받는다() {
		List<String> many = List.of("두부", "애호박", "동태", "새우", "깻잎", "고추", "돼지고기", "밀가루", "계란",
				"부추", "양파", "당근", "버섯", "소금", "식용유");
		aiAnswers(new AiIngredientEstimator.Estimated(many));

		assertEquals(many.subList(0, 12), estimator.estimate("모둠전"));
	}
}
