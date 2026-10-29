package com.nullteam.foodcheck.domain.menu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.nullteam.foodcheck.domain.image.service.ImageBodyStore;
import com.nullteam.foodcheck.domain.image.service.ImageService;
import com.nullteam.foodcheck.domain.menu.dto.ImageType;
import com.nullteam.foodcheck.domain.menu.dto.MenuRecognizeRequest;
import com.nullteam.foodcheck.domain.menu.dto.MenuRecognizeResponse;
import com.nullteam.foodcheck.global.exception.ApiException;

class MenuServiceTest {

	private final ImageService imageService = mock(ImageService.class);

	@BeforeEach
	void 사진이_있다() {
		when(imageService.loadBody("img_1")).thenReturn(new ImageBodyStore.Body(new byte[] { 1 }, "image/jpeg", Instant.now()));
	}

	@Test
	void 메뉴판은_읽은_순서대로_index를_매기고_못_읽은_칸은_null로_둔다() {
		MenuService service = service(new MenuReading(MenuReading.Kind.MENU_BOARD, Arrays.asList(" 제육볶음 ", "", null, "미역국")));

		MenuRecognizeResponse response = service.recognize(new MenuRecognizeRequest("img_1"));

		assertEquals(new MenuRecognizeResponse("img_1", ImageType.MENU_BOARD, List.of(
				new MenuRecognizeResponse.Menu(0, "제육볶음"),
				new MenuRecognizeResponse.Menu(1, null),
				new MenuRecognizeResponse.Menu(2, null),
				new MenuRecognizeResponse.Menu(3, "미역국"))), response);
	}

	@Test
	void 음식_사진은_요리명_1개만_돌려준다() {
		MenuService service = service(new MenuReading(MenuReading.Kind.FOOD, List.of("설렁탕", "깍두기")));

		MenuRecognizeResponse response = service.recognize(new MenuRecognizeRequest("img_1"));

		assertEquals(ImageType.FOOD, response.imageType());
		assertEquals(List.of(new MenuRecognizeResponse.Menu(0, "설렁탕")), response.menus());
	}

	@Test
	void 메뉴가_없으면_빈_목록이다() {
		MenuService service = service(new MenuReading(MenuReading.Kind.MENU_BOARD, null));

		assertEquals(List.of(), service.recognize(new MenuRecognizeRequest("img_1")).menus());
	}

	@Test
	void 메뉴판도_음식도_아니면_422() {
		MenuService service = service(new MenuReading(MenuReading.Kind.NOT_MENU, List.of()));

		ApiException e = assertThrows(ApiException.class, () -> service.recognize(new MenuRecognizeRequest("img_1")));
		assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, e.getStatus());
		assertEquals("This doesn't look like a menu or food photo.", e.getMessage());
	}

	@Test
	void AI_오류는_500() {
		MenuService service = new MenuService(imageService, (image, mime) -> {
			throw new IllegalStateException("gateway down");
		});

		ApiException e = assertThrows(ApiException.class, () -> service.recognize(new MenuRecognizeRequest("img_1")));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
	}

	@Test
	void AI_시간_초과는_500() {
		MenuService service = new MenuService(imageService, (image, mime) -> {
			try {
				Thread.sleep(2_000);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
			return new MenuReading(MenuReading.Kind.FOOD, List.of("설렁탕"));
		}, Duration.ofMillis(100));

		ApiException e = assertThrows(ApiException.class, () -> service.recognize(new MenuRecognizeRequest("img_1")));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
	}

	@Test
	void 형식이_틀린_AI_응답은_500() {
		MenuService service = service(new MenuReading(null, List.of("설렁탕")));

		ApiException e = assertThrows(ApiException.class, () -> service.recognize(new MenuRecognizeRequest("img_1")));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
	}

	@Test
	void image_id가_없으면_400() {
		MenuService service = service(new MenuReading(MenuReading.Kind.FOOD, List.of("설렁탕")));

		assertEquals(HttpStatus.BAD_REQUEST,
				assertThrows(ApiException.class, () -> service.recognize(new MenuRecognizeRequest(" "))).getStatus());
		assertEquals(HttpStatus.BAD_REQUEST,
				assertThrows(ApiException.class, () -> service.recognize(null)).getStatus());
	}

	private MenuService service(MenuReading reading) {
		return new MenuService(imageService, (image, mime) -> reading);
	}
}
