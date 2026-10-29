package com.nullteam.foodcheck.domain.judgment.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.ingredient.dto.IngredientView;
import com.nullteam.foodcheck.domain.ingredient.service.IngredientInferenceService;
import com.nullteam.foodcheck.domain.ingredient.service.MenuNameNormalizer;
import com.nullteam.foodcheck.domain.judgment.dto.JudgmentRequest;
import com.nullteam.foodcheck.domain.judgment.dto.JudgmentResponse;
import com.nullteam.foodcheck.domain.judgment.dto.JudgmentResult;
import com.nullteam.foodcheck.domain.judgment.dto.ManualJudgmentRequest;
import com.nullteam.foodcheck.domain.restriction.dto.PresetListResponse;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * REQ-3 3단계 판정과 OPT-1 수동 입력 판정. 재료는 REQ-5 추론으로 찾고, 판정은 JudgmentRules가 한다.
 *
 * <p>사용자 제한은 RestrictionService.findEnabledItemCodes로만 받는다(docs/roles.md 충돌 방지 규칙).
 * 등록되지 않은 user_id도 404가 아니라 빈 제한으로 보고 R0으로 판정한다.
 */
@Service
public class JudgmentService {

	private static final String EMPTY_INPUT = "입력 값이 없습니다.";

	private final IngredientInferenceService ingredientInferenceService;
	private final RestrictionService restrictionService;

	public JudgmentService(IngredientInferenceService ingredientInferenceService,
			RestrictionService restrictionService) {
		this.ingredientInferenceService = ingredientInferenceService;
		this.restrictionService = restrictionService;
	}

	@Transactional
	public JudgmentResponse judge(JudgmentRequest request) {
		List<String> menuNames = requireMenuNames(request);
		Set<String> enabled = enabledItems(request.userId());
		Map<String, String> itemNames = itemNames();

		return new JudgmentResponse(menuNames.stream()
				.map(name -> judgeOne(name, enabled, itemNames))
				.toList());
	}

	@Transactional
	public JudgmentResult judgeManual(ManualJudgmentRequest request) {
		if (request == null || MenuNameNormalizer.normalize(request.menuName()).isEmpty()) {
			throw ApiException.badRequest(EMPTY_INPUT);
		}
		return judgeOne(request.menuName(), enabledItems(request.userId()), itemNames());
	}

	private JudgmentResult judgeOne(String menuName, Set<String> enabled, Map<String, String> itemNames) {
		// 음식 DB에 없는 메뉴도 404가 아니다. REQ-5 순서로 재료를 찾아 판정한다.
		List<IngredientView> ingredients = ingredientInferenceService.infer(menuName);
		return JudgmentRules.apply(MenuNameNormalizer.normalize(menuName), enabled, ingredients, itemNames);
	}

	private static List<String> requireMenuNames(JudgmentRequest request) {
		if (request == null || request.menuNames() == null || request.menuNames().isEmpty()) {
			throw ApiException.badRequest(EMPTY_INPUT);
		}
		if (request.menuNames().stream().anyMatch(name -> MenuNameNormalizer.normalize(name).isEmpty())) {
			throw ApiException.badRequest(EMPTY_INPUT);
		}
		return request.menuNames();
	}

	/** 사용자가 없거나 식단 제한을 저장한 적 없으면 빈 집합이다(R0). */
	private Set<String> enabledItems(Long userId) {
		return restrictionService.findEnabledItemCodes(userId).orElseGet(Set::of);
	}

	/** 근거 한 줄에 "돼지고기"처럼 화면과 같은 이름을 쓰려고 제한 항목 이름을 가져온다. */
	private Map<String, String> itemNames() {
		return restrictionService.listPresets().items().stream()
				.collect(Collectors.toMap(PresetListResponse.Item::code, PresetListResponse.Item::name));
	}
}
