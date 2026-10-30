package com.nullteam.foodcheck.domain.menu.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

/** REQ-2 Spring AI 비전 모델로 사진을 읽어 구조화 출력(MenuReading)으로 받는다. */
@Component
public class SpringAiMenuReader implements MenuReader {

	static final String SYSTEM_PROMPT = """
			너는 한국 대학교 학식당의 메뉴판과 음식 사진을 읽는다. 아래 규칙대로만 답한다.
			1. 메뉴 이름이 적힌 메뉴판(판, 종이, 화면, 식단표)이면 kind는 MENU_BOARD다. 적힌 메뉴 이름을 위에서 아래, 왼쪽에서 오른쪽 순서로 전부 menus에 넣는다.
			2. 음식 사진이면 kind는 FOOD다. 가운데에 있는 주된 요리 이름 1개만 menus에 넣는다.
			3. 메뉴판도 음식 사진도 아니면 kind는 NOT_MENU이고 menus는 빈 배열이다.
			4. 글자가 흐리거나 가려져 읽을 수 없는 메뉴는 추측하지 말고 그 자리에 빈 문자열 ""을 넣는다. 음식 사진인데 어떤 요리인지 확실하지 않으면 menus에 빈 문자열 하나만 넣는다.
			5. 메뉴 이름만 적는다. 가격, 칼로리, 원산지, 날짜, 요일, 코너 이름, 괄호 속 설명, 분량(3g, 1인분)은 뺀다. 메뉴판에 적힌 한국어 표기를 그대로 쓰고, 음식 사진은 흔히 쓰는 한국어 요리 이름으로 쓴다.
			6. 메뉴판 제목, 식당·카페 이름, 코스 이름(런치 코스, Menu, 오늘의 메뉴 등), 코스 수(7 services 등), 분류 제목(COLD BEVERAGES 등)은 메뉴가 아니다.
			7. 한 메뉴가 한국어와 외국어로 함께 적혀 있으면 한국어 표기 하나만 넣는다. 같은 메뉴를 두 번 넣지 않는다. 한국어 표기가 없는 메뉴만 적힌 그대로 넣는다.
			8. 메뉴 이름 아래나 옆에 작은 글씨로 적힌 재료·설명 줄은 메뉴가 아니다. 단, 7번처럼 외국어 메뉴의 한국어 표기라면 그 한국어 줄이 메뉴 이름이다.
			9. 한 글자 메뉴(떡, 죽 등)도 메뉴다. 글씨가 작아도 맨 위 첫 메뉴부터 맨 아래 마지막 메뉴까지 하나씩 빠짐없이 넣는다.
			""";

	private final ChatClient chatClient;
	private final String model;

	public SpringAiMenuReader(ChatClient.Builder chatClientBuilder,
			// TODO(확인): AI Gateway에서 쓸 비전 모델. 팀 예산($10)과 판독 품질을 보고 정한다.
			@Value("${MENU_AI_MODEL:openai/gpt-5-mini}") String model) {
		this.chatClient = chatClientBuilder.build();
		this.model = model;
	}

	@Override
	public MenuReading read(byte[] image, String mimeType) {
		return chatClient.prompt()
				.options(OpenAiChatOptions.builder().model(model))
				.system(SYSTEM_PROMPT)
				.user(u -> u.text("이 사진에 있는 메뉴 이름을 읽어 줘.")
						.media(MimeTypeUtils.parseMimeType(mimeType), new ByteArrayResource(image)))
				.call()
				.entity(MenuReading.class);
	}
}
