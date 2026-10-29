package com.nullteam.foodcheck.domain.ingredient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import com.nullteam.foodcheck.domain.ingredient.dto.Basis;
import com.nullteam.foodcheck.domain.ingredient.dto.Confidence;
import com.nullteam.foodcheck.domain.ingredient.dto.Evidence;
import com.nullteam.foodcheck.domain.ingredient.dto.IngredientView;
import com.nullteam.foodcheck.domain.ingredient.entity.Food;
import com.nullteam.foodcheck.domain.ingredient.entity.FoodIngredient;
import com.nullteam.foodcheck.domain.ingredient.entity.Ingredient;
import com.nullteam.foodcheck.domain.ingredient.repository.FoodRepository;
import com.nullteam.foodcheck.domain.ingredient.repository.IngredientRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

/** REQ-5 찾는 순서(docs/api.md > REQ-5): 음식 DB → 만개의 레시피 → AI 추정, 이름·조리법 단어는 덧붙인다. */
class IngredientInferenceServiceTest {

	private final FoodRepository foodRepository = mock(FoodRepository.class);
	private final IngredientRepository ingredientRepository = mock(IngredientRepository.class);
	private final RecipeSearchClient recipeSearchClient = mock(RecipeSearchClient.class);
	private final AiIngredientEstimator aiIngredientEstimator = mock(AiIngredientEstimator.class);

	private final IngredientInferenceService service = new IngredientInferenceService(foodRepository,
			ingredientRepository, recipeSearchClient, aiIngredientEstimator);

	@BeforeEach
	void 저장은_넘긴_값을_돌려준다() {
		when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(i -> i.getArgument(0));
		when(foodRepository.save(any(Food.class))).thenAnswer(i -> i.getArgument(0));
	}

	@Test
	void 음식_DB에_있으면_등록_재료는_CONFIRMED_DB이고_외부는_부르지_않는다() {
		when(foodRepository.findByName("제육볶음")).thenReturn(Optional.of(food("제육볶음",
				confirmed("돼지고기", Set.of("pork", "meat")), confirmed("고추장", Set.of()))));

		List<IngredientView> views = service.infer(" 제육볶음 ");

		assertEquals(List.of(
				new IngredientView("돼지고기", Evidence.CONFIRMED, Confidence.HIGH, Basis.DB, List.of("meat", "pork")),
				new IngredientView("고추장", Evidence.CONFIRMED, Confidence.HIGH, Basis.DB, List.of()),
				new IngredientView("맛술", Evidence.INFERRED, Confidence.LOW, Basis.COOKING_METHOD, List.of("alcohol"))),
				views);
		verify(recipeSearchClient, never()).search(anyString());
		verify(aiIngredientEstimator, never()).estimate(anyString());
		verify(foodRepository, never()).save(any(Food.class));
	}

	@Test
	void 띄어쓰기만_다른_이름도_음식_DB에서_찾는다() {
		when(foodRepository.findByCompactName("제육볶음")).thenReturn(Optional.of(food("제육볶음",
				confirmed("돼지고기", Set.of("pork", "meat")))));

		List<IngredientView> views = service.infer("제육 볶음");

		assertEquals(Basis.DB, views.get(0).basis());
		assertEquals(Evidence.CONFIRMED, views.get(0).evidence());
		verify(recipeSearchClient, never()).search(anyString());
	}

	@Test
	void 음식_DB에_없으면_만개의_레시피_결과를_INFERRED로_쓰고_저장하며_AI는_부르지_않는다() {
		when(recipeSearchClient.search("비빔밥")).thenReturn(List.of("계란", "고추장"));

		List<IngredientView> views = service.infer("비빔밥");

		assertEquals(List.of(
				new IngredientView("계란", Evidence.INFERRED, Confidence.HIGH, Basis.RECIPE_SEARCH, List.of("egg")),
				new IngredientView("고추장", Evidence.INFERRED, Confidence.HIGH, Basis.RECIPE_SEARCH, List.of())),
				views);
		verify(aiIngredientEstimator, never()).estimate(anyString());
		assertEquals("RECIPE_SEARCH", savedFood().getSource());
	}

	@Test
	void 만개의_레시피에도_없으면_AI_추정이고_confidence는_MEDIUM이다() {
		when(recipeSearchClient.search("비빔밥")).thenReturn(List.of());
		when(aiIngredientEstimator.estimate("비빔밥")).thenReturn(List.of("계란"));

		List<IngredientView> views = service.infer("비빔밥");

		assertEquals(List.of(
				new IngredientView("계란", Evidence.INFERRED, Confidence.MEDIUM, Basis.AI_ESTIMATE, List.of("egg"))),
				views);
		Food saved = savedFood();
		assertEquals("AI_ESTIMATE", saved.getSource());
		assertEquals(Basis.AI_ESTIMATE, saved.getIngredients().get(0).getBasis());
	}

	@Test
	void 사전에_있는_재료는_사전의_제한_항목을_쓴다() {
		when(recipeSearchClient.search("비빔밥")).thenReturn(List.of("맛간장"));
		when(ingredientRepository.findByName("맛간장")).thenReturn(Optional.of(Ingredient.of("맛간장", Set.of("wheat"))));

		assertEquals(List.of("wheat"), service.infer("비빔밥").get(0).restrictions());
	}

	@Test
	void 외부에서_찾은_재료가_이름_단어와_겹치면_외부_결과를_남긴다() {
		when(recipeSearchClient.search("돈가스")).thenReturn(List.of("돼지고기", "빵가루"));

		List<IngredientView> views = service.infer("돈가스");

		assertEquals(List.of("돼지고기", "빵가루", "밀가루", "계란"), views.stream().map(IngredientView::name).toList());
		assertEquals(Basis.RECIPE_SEARCH, views.get(0).basis());
		assertEquals(Basis.COOKING_METHOD, views.get(2).basis());
	}

	@Test
	void 아무데서도_못_찾으면_빈_목록이고_저장하지_않는다() {
		when(recipeSearchClient.search("비빔밥")).thenReturn(List.of());
		when(aiIngredientEstimator.estimate("비빔밥")).thenReturn(List.of());

		assertEquals(List.of(), service.infer("비빔밥"));
		verify(foodRepository, never()).save(any(Food.class));
	}

	@Test
	void 같은_메뉴가_먼저_저장돼_있어도_찾은_재료로_판정을_이어간다() {
		when(recipeSearchClient.search("비빔밥")).thenReturn(List.of("계란"));
		when(foodRepository.save(any(Food.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

		assertEquals(List.of("계란"), service.infer("비빔밥").stream().map(IngredientView::name).toList());
	}

	@Test
	void 빈_메뉴_이름은_infer가_빈_목록이고_inferResponse는_400이다() {
		assertEquals(List.of(), service.infer("  "));

		ApiException e = assertThrows(ApiException.class, () -> service.inferResponse(" "));
		assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
		assertEquals("Please enter a menu name.", e.getMessage());
	}

	private Food savedFood() {
		ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
		verify(foodRepository).save(captor.capture());
		return captor.getValue();
	}

	private static Food food(String name, FoodIngredient... ingredients) {
		return Food.of(name, "SEED", List.of(ingredients));
	}

	private static FoodIngredient confirmed(String name, Set<String> restrictions) {
		return FoodIngredient.of(Ingredient.of(name, restrictions), Evidence.CONFIRMED, Confidence.HIGH, Basis.DB);
	}
}
