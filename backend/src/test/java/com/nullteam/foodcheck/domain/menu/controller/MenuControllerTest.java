package com.nullteam.foodcheck.domain.menu.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.nullteam.foodcheck.domain.image.service.ImageBodyStore;
import com.nullteam.foodcheck.domain.image.service.ImageService;
import com.nullteam.foodcheck.domain.menu.service.MenuReader;
import com.nullteam.foodcheck.domain.menu.service.MenuReading;
import com.nullteam.foodcheck.domain.menu.service.MenuService;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * REQ-2 메뉴 판독을 HTTP로 부른다. 판독 서비스는 진짜를 쓰고, 사진 본문과 AI 판독기만 가짜로 둔다.
 * 응답 필드 이름·상태 코드·에러 문구는 docs/api.md > REQ-2와 같아야 한다.
 */
@WebMvcTest(MenuController.class)
@Import(MenuService.class)
class MenuControllerTest {

	private static final String URL = "/api/v1/menus/recognize";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ImageService imageService;

	@MockitoBean
	private MenuReader menuReader;

	@BeforeEach
	void 사진이_하나_있다() {
		when(imageService.loadBody(anyString())).thenThrow(ApiException.notFound("Image not found."));
		doReturn(new ImageBodyStore.Body(new byte[] { 1 }, "image/jpeg", Instant.now()))
				.when(imageService).loadBody("img_1");
	}

	@Test
	void 메뉴판은_snake_case로_돌려주고_못_읽은_칸은_name이_null이다() throws Exception {
		when(menuReader.read(any(), any()))
				.thenReturn(new MenuReading(MenuReading.Kind.MENU_BOARD, Arrays.asList("제육볶음", null, "미역국")));

		recognize("{\"image_id\":\"img_1\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.image_id").value("img_1"))
				.andExpect(jsonPath("$.image_type").value("MENU_BOARD"))
				.andExpect(jsonPath("$.menus.length()").value(3))
				.andExpect(jsonPath("$.menus[0].index").value(0))
				.andExpect(jsonPath("$.menus[0].name").value("제육볶음"))
				.andExpect(jsonPath("$.menus[1].index").value(1))
				.andExpect(jsonPath("$.menus[1].name").value(nullValue()))
				.andExpect(jsonPath("$.menus[2].name").value("미역국"));
	}

	@Test
	void 음식_사진은_FOOD와_요리명_1개다() throws Exception {
		when(menuReader.read(any(), any())).thenReturn(new MenuReading(MenuReading.Kind.FOOD, List.of("설렁탕", "깍두기")));

		recognize("{\"image_id\":\"img_1\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.image_type").value("FOOD"))
				.andExpect(jsonPath("$.menus.length()").value(1))
				.andExpect(jsonPath("$.menus[0].name").value("설렁탕"));
	}

	@Test
	void image_id가_없으면_400이고_판독하지_않는다() throws Exception {
		recognize("{}").andExpect(status().isBadRequest());

		verifyNoInteractions(menuReader);
	}

	@Test
	void 없는_image_id는_404다() throws Exception {
		recognize("{\"image_id\":\"img_missing\"}")
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Image not found."));

		verifyNoInteractions(menuReader);
	}

	@Test
	void 메뉴판도_음식도_아니면_422다() throws Exception {
		when(menuReader.read(any(), any())).thenReturn(new MenuReading(MenuReading.Kind.NOT_MENU, List.of()));

		recognize("{\"image_id\":\"img_1\"}")
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("This doesn't look like a menu or food photo."));
	}

	@Test
	void AI_오류는_500과_수동_입력_안내다() throws Exception {
		when(menuReader.read(any(), any())).thenThrow(new IllegalStateException("gateway down"));

		recognize("{\"image_id\":\"img_1\"}")
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.detail").value("We couldn't read the menu. Please type the menu name."));
	}

	private ResultActions recognize(String body) throws Exception {
		return mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body));
	}
}
