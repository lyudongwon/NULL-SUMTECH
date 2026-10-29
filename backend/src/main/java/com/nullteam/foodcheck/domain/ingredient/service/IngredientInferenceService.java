package com.nullteam.foodcheck.domain.ingredient.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.ingredient.dto.Basis;
import com.nullteam.foodcheck.domain.ingredient.dto.Confidence;
import com.nullteam.foodcheck.domain.ingredient.dto.Evidence;
import com.nullteam.foodcheck.domain.ingredient.dto.IngredientInferResponse;
import com.nullteam.foodcheck.domain.ingredient.dto.IngredientView;
import com.nullteam.foodcheck.domain.ingredient.entity.Food;
import com.nullteam.foodcheck.domain.ingredient.entity.FoodIngredient;
import com.nullteam.foodcheck.domain.ingredient.entity.Ingredient;
import com.nullteam.foodcheck.domain.ingredient.repository.FoodRepository;
import com.nullteam.foodcheck.domain.ingredient.repository.IngredientRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * REQ-5 숨은 성분 추론. docs/api.md > REQ-5 > 찾는 순서를 그대로 따른다.
 *
 * <ol>
 * <li>음식 DB: 등록된 재료는 CONFIRMED + DB, 시드에 함께 넣은 숨은 재료는 INFERRED + COOKING_METHOD
 * <li>이름 속 재료 단어(NAME_KEYWORD)와 조리법(COOKING_METHOD): 모두 INFERRED
 * <li>만개의 레시피(1에 없을 때): INFERRED + RECIPE_SEARCH
 * <li>AI 추정(3이 실패하거나 결과가 없을 때): INFERRED + AI_ESTIMATE, confidence는 최대 MEDIUM
 * </ol>
 *
 * 3·4에서 찾은 재료는 foods에 저장해 다음 요청부터 1에서 찾는다.
 */
@Service
public class IngredientInferenceService {

	private static final Logger log = LoggerFactory.getLogger(IngredientInferenceService.class);

	private final FoodRepository foodRepository;
	private final IngredientRepository ingredientRepository;
	private final RecipeSearchClient recipeSearchClient;
	private final AiIngredientEstimator aiIngredientEstimator;

	public IngredientInferenceService(FoodRepository foodRepository, IngredientRepository ingredientRepository,
			RecipeSearchClient recipeSearchClient, AiIngredientEstimator aiIngredientEstimator) {
		this.foodRepository = foodRepository;
		this.ingredientRepository = ingredientRepository;
		this.recipeSearchClient = recipeSearchClient;
		this.aiIngredientEstimator = aiIngredientEstimator;
	}

	@Transactional
	public IngredientInferResponse inferResponse(String menuName) {
		requireMenuName(menuName);
		return new IngredientInferResponse(MenuNameNormalizer.normalize(menuName), infer(menuName));
	}

	/** REQ-3·OPT-1 판정이 부르는 함수. 재료를 하나도 찾지 못하면 빈 목록이다(판정은 R3). */
	@Transactional
	public List<IngredientView> infer(String menuName) {
		String name = MenuNameNormalizer.normalize(menuName);
		if (name.isEmpty()) {
			return List.of();
		}

		List<IngredientView> fromDb = findFood(name).map(IngredientInferenceService::toViews).orElse(List.of());
		List<IngredientView> fromName = guessFromMenuName(name);

		if (!fromDb.isEmpty()) {
			return merge(fromDb, fromName);
		}

		// 음식 DB에 없는 메뉴다. 만개의 레시피 → AI 추정 순으로 찾고 찾은 것을 저장한다.
		List<IngredientView> found = fromExternal(name, recipeSearchClient.search(name), Basis.RECIPE_SEARCH,
				Confidence.HIGH);
		if (found.isEmpty()) {
			found = fromExternal(name, aiIngredientEstimator.estimate(name), Basis.AI_ESTIMATE, Confidence.MEDIUM);
		}
		return merge(found, fromName);
	}

	static void requireMenuName(String menuName) {
		if (MenuNameNormalizer.normalize(menuName).isEmpty()) {
			throw ApiException.badRequest("Please enter a menu name.");
		}
	}

	private Optional<Food> findFood(String name) {
		return foodRepository.findByName(name)
				.or(() -> foodRepository.findByCompactName(MenuNameNormalizer.compact(name)));
	}

	private static List<IngredientView> toViews(Food food) {
		return food.getIngredients().stream()
				.map(fi -> new IngredientView(fi.getIngredient().getName(), fi.getEvidence(), fi.getConfidence(),
						fi.getBasis(), List.copyOf(fi.getIngredient().getRestrictionItems())))
				.toList();
	}

	private List<IngredientView> guessFromMenuName(String name) {
		List<IngredientView> views = new ArrayList<>();
		for (String ingredientName : IngredientKeywords.fromMenuName(name)) {
			views.add(view(ingredientName, Basis.NAME_KEYWORD, Confidence.HIGH));
		}
		for (String ingredientName : IngredientKeywords.fromCookingMethod(name)) {
			views.add(view(ingredientName, Basis.COOKING_METHOD, Confidence.LOW));
		}
		return views;
	}

	/** 만개의 레시피·AI 추정으로 찾은 재료를 뷰로 바꾸고 foods에 저장한다. */
	private List<IngredientView> fromExternal(String menuName, List<String> names, Basis basis,
			Confidence confidence) {
		if (names.isEmpty()) {
			return List.of();
		}
		List<IngredientView> views = names.stream().map(n -> view(n, basis, confidence)).toList();
		save(menuName, views, basis);
		return views;
	}

	private void save(String menuName, List<IngredientView> views, Basis basis) {
		try {
			List<FoodIngredient> rows = views.stream()
					.map(v -> FoodIngredient.of(findOrCreateIngredient(v.name()), v.evidence(), v.confidence(),
							v.basis()))
					.toList();
			foodRepository.save(Food.of(menuName, basis.name(), rows));
		} catch (DataIntegrityViolationException e) {
			// 같은 메뉴를 동시에 판정하면 한쪽만 저장된다. 판정은 이미 찾은 재료로 그대로 진행한다.
			log.info("음식 저장을 건너뛴다(이미 있음): menuName={}", menuName);
		}
	}

	private Ingredient findOrCreateIngredient(String name) {
		return ingredientRepository.findByName(name)
				.orElseGet(() -> ingredientRepository
						.save(Ingredient.of(name, IngredientKeywords.restrictionsOf(name))));
	}

	/** 사전에 있는 재료면 사전의 제한 항목을, 없으면 단어표로 찾은 항목을 쓴다. */
	private IngredientView view(String name, Basis basis, Confidence confidence) {
		Set<String> restrictions = ingredientRepository.findByName(name)
				.map(Ingredient::getRestrictionItems)
				.orElseGet(() -> IngredientKeywords.restrictionsOf(name));
		return new IngredientView(name, Evidence.INFERRED, confidence, basis, List.copyOf(restrictions));
	}

	/** 같은 재료가 여러 단계에서 나오면 앞 단계(음식 DB → 외부 → 이름·조리법)를 남긴다. */
	private static List<IngredientView> merge(List<IngredientView> first, List<IngredientView> second) {
		Map<String, IngredientView> byName = new LinkedHashMap<>();
		first.forEach(v -> byName.putIfAbsent(v.name(), v));
		second.forEach(v -> byName.putIfAbsent(v.name(), v));
		return List.copyOf(byName.values());
	}
}
