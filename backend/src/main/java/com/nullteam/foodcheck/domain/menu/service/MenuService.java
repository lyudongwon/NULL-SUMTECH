package com.nullteam.foodcheck.domain.menu.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nullteam.foodcheck.domain.image.service.ImageBodyStore;
import com.nullteam.foodcheck.domain.image.service.ImageService;
import com.nullteam.foodcheck.domain.menu.dto.ImageType;
import com.nullteam.foodcheck.domain.menu.dto.MenuRecognizeRequest;
import com.nullteam.foodcheck.domain.menu.dto.MenuRecognizeResponse;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * REQ-2 메뉴 판독. 읽지 못한 메뉴는 추측하지 않고 name을 null로 둔다.
 * 판독 결과는 저장하지 않는다(docs/erd.md 전제).
 */
@Service
public class MenuService {

	static final Duration TIMEOUT = Duration.ofSeconds(45);
	static final int MAX_MENUS = 30;
	static final String READ_FAILED = "메뉴를 읽지 못했습니다. 메뉴 이름을 직접 입력해 주세요.";

	private static final Logger log = LoggerFactory.getLogger(MenuService.class);

	private final ImageService imageService;
	private final MenuReader menuReader;
	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	private final Duration timeout;

	@Autowired
	public MenuService(ImageService imageService, MenuReader menuReader) {
		this(imageService, menuReader, TIMEOUT);
	}

	MenuService(ImageService imageService, MenuReader menuReader, Duration timeout) {
		this.imageService = imageService;
		this.menuReader = menuReader;
		this.timeout = timeout;
	}

	public MenuRecognizeResponse recognize(MenuRecognizeRequest request) {
		String imageId = request == null ? null : request.imageId();
		if (imageId == null || imageId.isBlank()) {
			throw ApiException.badRequest("image_id 값이 필요합니다.");
		}
		ImageBodyStore.Body body = imageService.loadBody(imageId.trim());

		MenuReading reading = read(body);
		if (reading == null || reading.kind() == null) {
			log.warn("판독 결과 형식이 올바르지 않음: {}", reading);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, READ_FAILED);
		}
		if (reading.kind() == MenuReading.Kind.NOT_MENU) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "메뉴판이나 음식 사진이 아닙니다.");
		}

		ImageType type = reading.kind() == MenuReading.Kind.FOOD ? ImageType.FOOD : ImageType.MENU_BOARD;
		List<String> names = reading.menus() == null ? List.of() : reading.menus();
		int limit = type == ImageType.FOOD ? 1 : MAX_MENUS;

		List<MenuRecognizeResponse.Menu> menus = new ArrayList<>();
		for (String name : names) {
			if (menus.size() == limit) {
				break;
			}
			menus.add(new MenuRecognizeResponse.Menu(menus.size(), clean(name)));
		}
		return new MenuRecognizeResponse(imageId.trim(), type, menus);
	}

	/** AI 오류·시간 초과는 500으로 바꾼다. 화면은 이때 메뉴 이름 입력칸(OPT-1)을 보여 준다. */
	private MenuReading read(ImageBodyStore.Body body) {
		try {
			return CompletableFuture.supplyAsync(() -> menuReader.read(body.bytes(), body.mimeType()), executor)
					.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
					.join();
		} catch (CompletionException e) {
			log.warn("메뉴 판독 실패", e.getCause() == null ? e : e.getCause());
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, READ_FAILED);
		}
	}

	/** 앞뒤 공백을 지우고, 비었으면 읽지 못한 것으로 보고 null을 돌려준다. */
	static String clean(String name) {
		if (name == null) {
			return null;
		}
		String trimmed = name.strip();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
