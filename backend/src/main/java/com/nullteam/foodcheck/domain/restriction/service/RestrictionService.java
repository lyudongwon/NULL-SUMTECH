package com.nullteam.foodcheck.domain.restriction.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nullteam.foodcheck.domain.restriction.dto.PresetListResponse;
import com.nullteam.foodcheck.domain.restriction.dto.RestrictionPatchRequest;
import com.nullteam.foodcheck.domain.restriction.dto.RestrictionResponse;
import com.nullteam.foodcheck.domain.restriction.entity.RestrictionItem;
import com.nullteam.foodcheck.domain.restriction.entity.RestrictionPreset;
import com.nullteam.foodcheck.domain.restriction.entity.UserRestriction;
import com.nullteam.foodcheck.domain.restriction.entity.UserRestrictionItem;
import com.nullteam.foodcheck.domain.restriction.repository.RestrictionItemRepository;
import com.nullteam.foodcheck.domain.restriction.repository.RestrictionPresetRepository;
import com.nullteam.foodcheck.domain.restriction.repository.UserRestrictionItemRepository;
import com.nullteam.foodcheck.domain.restriction.repository.UserRestrictionRepository;
import com.nullteam.foodcheck.domain.user.service.UserService;
import com.nullteam.foodcheck.global.exception.ApiException;

/** REQ-4 식단 제한. 다른 도메인(판정·음식점)은 findEnabledItemCodes로 사용자의 켜진 항목을 받는다. */
@Service
public class RestrictionService {

	private static final String CONFLICT = "다른 요청이 먼저 처리되었습니다. 다시 시도해 주세요.";

	private final RestrictionPresetRepository presetRepository;
	private final RestrictionItemRepository itemRepository;
	private final UserRestrictionRepository userRestrictionRepository;
	private final UserRestrictionItemRepository userItemRepository;
	private final UserService userService;

	public RestrictionService(RestrictionPresetRepository presetRepository, RestrictionItemRepository itemRepository,
			UserRestrictionRepository userRestrictionRepository, UserRestrictionItemRepository userItemRepository,
			UserService userService) {
		this.presetRepository = presetRepository;
		this.itemRepository = itemRepository;
		this.userRestrictionRepository = userRestrictionRepository;
		this.userItemRepository = userItemRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public PresetListResponse listPresets() {
		List<PresetListResponse.Preset> presets = presetRepository.findAll().stream()
				.sorted(Comparator.comparing(RestrictionPreset::getCode))
				.map(p -> new PresetListResponse.Preset(p.getCode(), p.getName(), List.copyOf(new TreeSet<>(p.getItems()))))
				.toList();
		List<PresetListResponse.Item> items = itemRepository.findAll().stream()
				.sorted(Comparator.comparing(RestrictionItem::getCode))
				.map(i -> new PresetListResponse.Item(i.getCode(), i.getName()))
				.toList();
		return new PresetListResponse(presets, items);
	}

	@Transactional(readOnly = true)
	public RestrictionResponse get(Long userId) {
		return read(userId).orElseThrow(() -> ApiException.notFound("저장된 식단 제한이 없습니다."));
	}

	@Transactional
	public RestrictionResponse patch(Long userId, RestrictionPatchRequest request) {
		userService.requireExists(userId);

		Set<String> allItemCodes = itemRepository.findAll().stream()
				.map(RestrictionItem::getCode)
				.collect(Collectors.toCollection(TreeSet::new));
		for (String code : request.toggles().keySet()) {
			if (!allItemCodes.contains(code)) {
				throw ApiException.badRequest("존재하지 않는 토글 항목입니다: " + code);
			}
		}
		Set<String> presetItems = null;
		if (request.presetGiven() && request.preset() != null) {
			RestrictionPreset preset = presetRepository.findById(request.preset())
					.orElseThrow(() -> ApiException.badRequest("존재하지 않는 프리셋입니다: " + request.preset()));
			presetItems = Set.copyOf(preset.getItems());
		}

		Optional<UserRestriction> existing = userRestrictionRepository.findById(userId);
		Map<String, Boolean> current;
		if (existing.isEmpty()) {
			// 처음 저장: version은 생략해야 한다. 같은 user_id가 동시에 들어오면 한쪽만 성공한다.
			if (request.version() != null
					|| userRestrictionRepository.insertIfAbsent(userId, request.preset()) == 0) {
				throw ApiException.conflict(CONFLICT);
			}
			current = Map.of();
		} else {
			UserRestriction row = existing.get();
			if (request.version() == null || request.version() != row.getVersion()) {
				throw ApiException.conflict(CONFLICT);
			}
			String presetCode = request.presetGiven() ? request.preset() : row.getPresetCode();
			if (userRestrictionRepository.updateIfVersion(userId, presetCode, request.version()) == 0) {
				throw ApiException.conflict(CONFLICT);
			}
			current = toggles(userId);
		}

		Map<String, Boolean> merged = ToggleMerger.merge(current, presetItems, allItemCodes, request.toggles());
		merged.forEach((code, on) -> userItemRepository.upsert(userId, code, on));

		return read(userId).orElseThrow();
	}

	/** 판정·음식점 추천용. 사용자가 없으면 empty, 식단 제한을 저장하지 않았으면 빈 집합. */
	@Transactional(readOnly = true)
	public Optional<Set<String>> findEnabledItemCodes(Long userId) {
		if (!userService.exists(userId)) {
			return Optional.empty();
		}
		return Optional.of(toggles(userId).entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toCollection(TreeSet::new)));
	}

	private Optional<RestrictionResponse> read(Long userId) {
		return userRestrictionRepository.findById(userId)
				.map(r -> new RestrictionResponse(userId, r.getPresetCode(), toggles(userId), r.getVersion(),
						r.getUpdatedAt()));
	}

	private Map<String, Boolean> toggles(Long userId) {
		Map<String, Boolean> map = new LinkedHashMap<>();
		for (UserRestrictionItem item : userItemRepository.findByUserIdOrderByItemCode(userId)) {
			map.put(item.getItemCode(), item.isEnabled());
		}
		return map;
	}
}
