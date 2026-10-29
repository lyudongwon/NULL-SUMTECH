package com.nullteam.foodcheck.domain.ingredient.service;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * REQ-5 4단계. 만개의 레시피에서도 못 찾으면 AI로 재료를 추정한다. 결과는 항상 INFERRED이고
 * confidence는 최대 MEDIUM이라, AI 추정만으로는 "불가"가 나오지 않는다(docs/api.md REQ-3).
 *
 * <p>팀 AI 예산($10, docs/ai-rules.md 13번) 때문에 메뉴 하나에 한 번만 부르고, 찾은 재료는 foods에
 * 저장해 다음 요청부터는 음식 DB에서 찾는다(IngredientInferenceService).
 */
@Component
public class AiIngredientEstimator {

	private static final Logger log = LoggerFactory.getLogger(AiIngredientEstimator.class);

	private static final int MAX_INGREDIENTS = 12;

	private static final String PROMPT = """
			한국 학식당에서 파는 "%s"에 들어가는 재료를 알려 주세요.
			- 재료 이름만 한국어로 적습니다. 분량과 설명은 넣지 않습니다.
			- 겉으로 보이지 않지만 흔히 쓰이는 재료(육수, 젓갈, 맛술, 액젓, 햄 같은 것)를 꼭 포함합니다.
			- 아는 음식이 아니면 빈 목록을 돌려줍니다. 추측으로 채우지 않습니다.
			- 재료는 많아도 %d개까지입니다.
			""";

	/** 판정 요청 하나가 E2E 제한(120초)을 넘지 않게 끊는다. 메뉴 판독(MenuService)의 45초보다 짧게 둔다. */
	static final Duration TIMEOUT = Duration.ofSeconds(20);

	private final ObjectProvider<ChatClient.Builder> chatClientBuilder;
	private final String model;
	private final Duration timeout;
	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	@Autowired
	public AiIngredientEstimator(ObjectProvider<ChatClient.Builder> chatClientBuilder,
			// AI Gateway는 "제공사/모델" 이름만 받는다. 지정하지 않으면 Spring AI 기본 모델 이름(제공사 없음)이 나가서 실패한다.
			@Value("${INGREDIENT_AI_MODEL:openai/gpt-5-mini}") String model) {
		this(chatClientBuilder, model, TIMEOUT);
	}

	AiIngredientEstimator(ObjectProvider<ChatClient.Builder> chatClientBuilder, String model, Duration timeout) {
		this.chatClientBuilder = chatClientBuilder;
		this.model = model;
		this.timeout = timeout;
	}

	/** AI 호출이 안 되면(키 미설정·시간 초과·형식 오류) 빈 목록을 돌려준다. 판정은 R3으로 간다. */
	public List<String> estimate(String menuName) {
		ChatClient.Builder builder = chatClientBuilder.getIfAvailable();
		if (builder == null) {
			log.warn("AI 모델이 설정되지 않아 재료 추정을 건너뛴다: menuName={}", menuName);
			return List.of();
		}
		try {
			Estimated estimated = CompletableFuture.supplyAsync(() -> builder.build().prompt()
					.options(OpenAiChatOptions.builder().model(model))
					.user(PROMPT.formatted(menuName, MAX_INGREDIENTS))
					.call()
					.entity(Estimated.class), executor)
					.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
					.join();
			return clean(estimated);
		} catch (Exception e) {
			log.warn("AI 재료 추정 실패: menuName={}, cause={}", menuName, e.toString());
			return List.of();
		}
	}

	private static List<String> clean(Estimated estimated) {
		if (estimated == null || estimated.ingredients() == null) {
			return List.of();
		}
		Set<String> names = new LinkedHashSet<>();
		for (String raw : estimated.ingredients()) {
			String name = RecipeSearchClient.cleanName(raw == null ? "" : raw);
			if (!name.isEmpty()) {
				names.add(name);
			}
			if (names.size() >= MAX_INGREDIENTS) {
				break;
			}
		}
		return List.copyOf(names);
	}

	/** Spring AI 구조화 출력용. 재료 이름만 받는다. */
	record Estimated(List<String> ingredients) {
	}
}
