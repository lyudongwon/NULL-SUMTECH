package com.nullteam.foodcheck.domain.ingredient.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
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
import org.springframework.test.web.servlet.ResultActions;

import com.nullteam.foodcheck.domain.ingredient.dto.Basis;
import com.nullteam.foodcheck.domain.ingredient.dto.Confidence;
import com.nullteam.foodcheck.domain.ingredient.dto.Evidence;
import com.nullteam.foodcheck.domain.ingredient.entity.Food;
import com.nullteam.foodcheck.domain.ingredient.entity.FoodIngredient;
import com.nullteam.foodcheck.domain.ingredient.entity.Ingredient;
import com.nullteam.foodcheck.domain.ingredient.repository.FoodRepository;
import com.nullteam.foodcheck.domain.ingredient.repository.IngredientRepository;
import com.nullteam.foodcheck.domain.ingredient.service.AiIngredientEstimator;
import com.nullteam.foodcheck.domain.ingredient.service.IngredientInferenceService;
import com.nullteam.foodcheck.domain.ingredient.service.RecipeSearchClient;

/**
 * REQ-5 숨은 성분 추론을 HTTP로 부른다. 추론 서비스는 진짜를 쓰고, 저장소·만개의 레시피·AI 추정만 가짜로 둔다.
 * 응답 필드 이름·상태 코드·에러 문구는 docs/api.md > REQ-5와 같아야 한다.
 */
@WebMvcTest(IngredientController.class)
@Import(IngredientInferenceService.class)
class IngredientControllerTest {

	private static final String URL = "/api/v1/ingredients/infer";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private FoodRepository foodRepository;

	@MockitoBean
	private IngredientRepository ingredientRepository;

	@MockitoBean
	private RecipeSearchClient recipeSearchClient;

	@MockitoBean
	private AiIngredientEstimator aiIngredientEstimator;

	@BeforeEach
	void 저장은_넘긴_값을_돌려준다() {
		when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(i -> i.getArgument(0));
		when(foodRepository.save(any(Food.class))).thenAnswer(i -> i.getArgument(0));
	}

	@Test
	void 음식_DB의_재료를_snake_case와_근거_필드로_돌려준다() throws Exception {
		when(foodRepository.findByName("제육볶음")).thenReturn(Optional.of(Food.of("제육볶음", "SEED", List.of(
				confirmed("돼지고기", Set.of("pork", "meat")), confirmed("고추장", Set.of())))));

		infer("{\"menu_name\":\" 제육볶음 \"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.menu_name").value("제육볶음"))
				.andExpect(jsonPath("$.ingredients.length()").value(3))
				.andExpect(jsonPath("$.ingredients[0].name").value("돼지고기"))
				.andExpect(jsonPath("$.ingredients[0].evidence").value("CONFIRMED"))
				.andExpect(jsonPath("$.ingredients[0].confidence").value("HIGH"))
				.andExpect(jsonPath("$.ingredients[0].basis").value("DB"))
				.andExpect(jsonPath("$.ingredients[0].restrictions[0]").value("meat"))
				.andExpect(jsonPath("$.ingredients[0].restrictions[1]").value("pork"))
				.andExpect(jsonPath("$.ingredients[1].restrictions.length()").value(0))
				.andExpect(jsonPath("$.ingredients[2].name").value("맛술"))
				.andExpect(jsonPath("$.ingredients[2].evidence").value("INFERRED"))
				.andExpect(jsonPath("$.ingredients[2].basis").value("COOKING_METHOD"));

		verifyNoInteractions(recipeSearchClient, aiIngredientEstimator);
	}

	@Test
	void 만개의_레시피에_없으면_AI_추정이고_confidence는_MEDIUM이다() throws Exception {
		when(recipeSearchClient.search("비빔밥")).thenReturn(List.of());
		when(aiIngredientEstimator.estimate("비빔밥")).thenReturn(List.of("계란"));

		infer("{\"menu_name\":\"비빔밥\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ingredients.length()").value(1))
				.andExpect(jsonPath("$.ingredients[0].name").value("계란"))
				.andExpect(jsonPath("$.ingredients[0].evidence").value("INFERRED"))
				.andExpect(jsonPath("$.ingredients[0].confidence").value("MEDIUM"))
				.andExpect(jsonPath("$.ingredients[0].basis").value("AI_ESTIMATE"))
				.andExpect(jsonPath("$.ingredients[0].restrictions[0]").value("egg"));
	}

	@Test
	void 어디서도_재료를_못_찾으면_200과_빈_목록이다() throws Exception {
		infer("{\"menu_name\":\"zzz\"}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.menu_name").value("zzz"))
				.andExpect(jsonPath("$.ingredients.length()").value(0));

		verify(recipeSearchClient).search("zzz");
		verify(aiIngredientEstimator).estimate("zzz");
	}

	@Test
	void menu_name이_없거나_비었으면_400이고_재료를_찾지_않는다() throws Exception {
		for (String body : List.of("{}", "{\"menu_name\":\"\"}", "{\"menu_name\":\"   \"}")) {
			infer(body)
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value("Please enter a menu name."));
		}
		mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please enter a menu name."));

		verifyNoInteractions(recipeSearchClient, aiIngredientEstimator);
	}

	private ResultActions infer(String body) throws Exception {
		return mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	private static FoodIngredient confirmed(String name, Set<String> restrictions) {
		return FoodIngredient.of(Ingredient.of(name, restrictions), Evidence.CONFIRMED, Confidence.HIGH, Basis.DB);
	}
}
