package com.nullteam.foodcheck.domain.judgment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nullteam.foodcheck.domain.ingredient.dto.Basis;
import com.nullteam.foodcheck.domain.ingredient.dto.Confidence;
import com.nullteam.foodcheck.domain.ingredient.dto.Evidence;
import com.nullteam.foodcheck.domain.ingredient.dto.IngredientView;
import com.nullteam.foodcheck.domain.ingredient.service.IngredientInferenceService;
import com.nullteam.foodcheck.domain.judgment.service.JudgmentService;
import com.nullteam.foodcheck.domain.restriction.dto.PresetListResponse;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;

/**
 * REQ-3·REQ-6·OPT-1 판정 API를 HTTP로 부른다. 판정 서비스와 규칙은 진짜를 쓰고,
 * 재료 추론(REQ-5)과 사용자 제한(REQ-4)만 가짜로 둔다. 응답 필드 이름·에러 문구는 docs/api.md와 같아야 한다.
 */
@WebMvcTest(JudgmentController.class)
@Import(JudgmentService.class)
class JudgmentControllerTest {

	private static final IngredientView PORK = new IngredientView("돼지고기", Evidence.CONFIRMED, Confidence.HIGH, Basis.DB,
			List.of("pork", "meat"));
	private static final IngredientView RICE = new IngredientView("쌀", Evidence.CONFIRMED, Confidence.HIGH, Basis.DB,
			List.of());

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private IngredientInferenceService ingredientInferenceService;

	@MockitoBean
	private RestrictionService restrictionService;

	@BeforeEach
	void 할랄_사용자와_재료가_있다() {
		when(restrictionService.listPresets()).thenReturn(new PresetListResponse(List.of(),
				List.of(new PresetListResponse.Item("pork", "Pork"), new PresetListResponse.Item("alcohol", "Alcohol"))));
		when(restrictionService.findEnabledItemCodes(any())).thenReturn(Optional.empty());
		when(restrictionService.findEnabledItemCodes(7L)).thenReturn(Optional.of(Set.of("pork", "alcohol")));
		when(ingredientInferenceService.infer("제육볶음")).thenReturn(List.of(PORK, RICE));
		when(ingredientInferenceService.infer("공기밥")).thenReturn(List.of(RICE));
	}

	@Test
	void 메뉴마다_요청한_순서대로_판정과_근거를_snake_case로_돌려준다() throws Exception {
		mvc.perform(post("/api/v1/judgments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"menu_names\": [\"제육볶음\", \"공기밥\"]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.results.length()").value(2))
				.andExpect(jsonPath("$.results[0].menu_name").value("제육볶음"))
				.andExpect(jsonPath("$.results[0].level").value("BLOCKED"))
				.andExpect(jsonPath("$.results[0].rule").value("R1"))
				.andExpect(jsonPath("$.results[0].reason")
						.value("Contains 돼지고기, which is not allowed for your Pork restriction."))
				.andExpect(jsonPath("$.results[0].matched_restrictions[0]").value("pork"))
				.andExpect(jsonPath("$.results[0].ingredients[0].name").value("돼지고기"))
				.andExpect(jsonPath("$.results[0].ingredients[0].evidence").value("CONFIRMED"))
				.andExpect(jsonPath("$.results[0].ingredients[0].basis").value("DB"))
				.andExpect(jsonPath("$.results[1].menu_name").value("공기밥"))
				.andExpect(jsonPath("$.results[1].level").value("SAFE"))
				.andExpect(jsonPath("$.results[1].rule").value("R4"));
	}

	@Test
	void 등록되지_않은_user_id는_404가_아니라_R0_SAFE다() throws Exception {
		mvc.perform(post("/api/v1/judgments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 999, \"menu_names\": [\"제육볶음\"]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.results[0].level").value("SAFE"))
				.andExpect(jsonPath("$.results[0].rule").value("R0"))
				.andExpect(jsonPath("$.results[0].reason").value("You have no diet restrictions set."));
	}

	@Test
	void menu_names가_비었으면_400과_팝업_문구를_돌려준다() throws Exception {
		mvc.perform(post("/api/v1/judgments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"menu_names\": []}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please enter a menu name."));
		verifyNoInteractions(ingredientInferenceService);
	}

	@Test
	void menu_names에_빈_값이나_null이_있으면_400이다() throws Exception {
		mvc.perform(post("/api/v1/judgments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"menu_names\": [\"제육볶음\", \"  \"]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please enter a menu name."));
		mvc.perform(post("/api/v1/judgments").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"menu_names\": [null]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please enter a menu name."));
		verifyNoInteractions(ingredientInferenceService);
	}

	@Test
	void 바디가_없으면_400이다() throws Exception {
		mvc.perform(post("/api/v1/judgments").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please enter a menu name."));
	}

	@Test
	void 수동_입력은_메뉴_하나의_판정을_바로_돌려준다() throws Exception {
		mvc.perform(post("/api/v1/judgments/manual").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"menu_name\": \"제육볶음\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.menu_name").value("제육볶음"))
				.andExpect(jsonPath("$.level").value("BLOCKED"))
				.andExpect(jsonPath("$.rule").value("R1"));
	}

	@Test
	void 수동_입력의_menu_name이_비었으면_400이다() throws Exception {
		mvc.perform(post("/api/v1/judgments/manual").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7, \"menu_name\": \"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please enter a menu name."));
		mvc.perform(post("/api/v1/judgments/manual").contentType(MediaType.APPLICATION_JSON)
				.content("{\"user_id\": 7}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please enter a menu name."));
	}

	@Test
	void 지원하지_않는_메서드는_500이_아니라_405다() throws Exception {
		mvc.perform(get("/api/v1/judgments"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.detail").value("This method is not allowed."));
	}

	@Test
	void 깨진_JSON은_500이_아니라_400과_공통_문구다() throws Exception {
		mvc.perform(post("/api/v1/judgments").contentType(MediaType.APPLICATION_JSON).content("{\"user_id\": 7,"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("The request is not in a valid format."));
		verifyNoInteractions(ingredientInferenceService);
	}

	@Test
	void JSON이_아닌_본문은_500이_아니라_415다() throws Exception {
		mvc.perform(post("/api/v1/judgments").contentType(MediaType.TEXT_PLAIN).content("제육볶음"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.detail").value("This content type is not supported."));
		verifyNoInteractions(ingredientInferenceService);
	}
}
