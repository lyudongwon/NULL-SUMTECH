package com.nullteam.foodcheck.domain.restriction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nullteam.foodcheck.domain.restriction.entity.RestrictionItem;
import com.nullteam.foodcheck.domain.restriction.entity.UserRestriction;
import com.nullteam.foodcheck.domain.restriction.repository.RestrictionItemRepository;
import com.nullteam.foodcheck.domain.restriction.repository.RestrictionPresetRepository;
import com.nullteam.foodcheck.domain.restriction.repository.UserRestrictionItemRepository;
import com.nullteam.foodcheck.domain.restriction.repository.UserRestrictionRepository;
import com.nullteam.foodcheck.domain.restriction.service.RestrictionService;
import com.nullteam.foodcheck.domain.user.service.UserService;
import com.nullteam.foodcheck.global.exception.ApiException;

/**
 * REQ-4 저장(PATCH)의 낙관적 락을 HTTP로 확인한다. 서비스는 진짜를 쓰고 DB 저장소만 가짜로 둔다.
 * 조건부 쿼리가 0행을 고친 경우(다른 요청이 먼저 저장)를 저장소 반환값으로 흉내 낸다.
 */
@WebMvcTest(RestrictionController.class)
@Import(RestrictionService.class)
class RestrictionControllerTest {

	private static final String CONFLICT = "Another request was saved first. Please try again.";
	private static final String URL = "/api/v1/users/7/restrictions";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private RestrictionPresetRepository presetRepository;

	@MockitoBean
	private RestrictionItemRepository itemRepository;

	@MockitoBean
	private UserRestrictionRepository userRestrictionRepository;

	@MockitoBean
	private UserRestrictionItemRepository userItemRepository;

	@MockitoBean
	private UserService userService;

	@BeforeEach
	void 제한_항목이_있다() {
		RestrictionItem pork = mock(RestrictionItem.class);
		when(pork.getCode()).thenReturn("pork");
		when(itemRepository.findAll()).thenReturn(List.of(pork));
		when(userItemRepository.findByUserIdOrderByItemCode(anyLong())).thenReturn(List.of());
	}

	@Test
	void 조회한_version과_다르면_409와_다시_시도_문구를_돌려준다() throws Exception {
		저장된_행(3);

		mvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON)
				.content("{\"toggles\": {\"pork\": true}, \"version\": 2}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value(CONFLICT));
		verify(userRestrictionRepository, never()).updateIfVersion(anyLong(), any(), anyInt());
		verify(userItemRepository, never()).upsert(anyLong(), anyString(), anyBoolean());
	}

	@Test
	void 이미_저장한_적이_있는데_version을_빼면_409다() throws Exception {
		저장된_행(1);

		mvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content("{\"toggles\": {\"pork\": true}}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value(CONFLICT));
	}

	@Test
	void version이_같아도_다른_요청이_먼저_고쳤으면_409다() throws Exception {
		저장된_행(1);
		when(userRestrictionRepository.updateIfVersion(7L, null, 1)).thenReturn(0);

		mvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON)
				.content("{\"toggles\": {\"pork\": true}, \"version\": 1}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value(CONFLICT));
		verify(userItemRepository, never()).upsert(anyLong(), anyString(), anyBoolean());
	}

	@Test
	void 처음_저장이_동시에_들어와_한쪽이_먼저_넣었으면_409다() throws Exception {
		when(userService.exists(7L)).thenReturn(true);
		when(userRestrictionRepository.findById(7L)).thenReturn(Optional.empty());
		when(userRestrictionRepository.insertIfAbsent(7L, null)).thenReturn(0);

		mvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content("{\"toggles\": {\"pork\": true}}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value(CONFLICT));
	}

	@Test
	void 처음_저장에_version을_보내면_409다() throws Exception {
		when(userRestrictionRepository.findById(7L)).thenReturn(Optional.empty());

		mvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON)
				.content("{\"toggles\": {\"pork\": true}, \"version\": 1}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value(CONFLICT));
		verify(userRestrictionRepository, never()).insertIfAbsent(anyLong(), any());
	}

	@Test
	void version이_맞으면_저장하고_새_version을_돌려준다() throws Exception {
		UserRestriction before = 행(1);
		UserRestriction after = 행(2);
		when(userRestrictionRepository.findById(7L)).thenReturn(Optional.of(before), Optional.of(after));
		when(userRestrictionRepository.updateIfVersion(7L, null, 1)).thenReturn(1);

		mvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON)
				.content("{\"toggles\": {\"pork\": true}, \"version\": 1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user_id").value(7))
				.andExpect(jsonPath("$.version").value(2));
		verify(userItemRepository).upsert(7L, "pork", true);
	}

	@Test
	void 발급되지_않은_user_id는_404다() throws Exception {
		doThrow(ApiException.notFound("User not found.")).when(userService).requireExists(7L);

		mvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content("{\"toggles\": {\"pork\": true}}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("User not found."));
	}

	@Test
	void 없는_토글_코드는_400이다() throws Exception {
		mvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content("{\"toggles\": {\"unicorn\": true}}"))
				.andExpect(status().isBadRequest());
		verify(userRestrictionRepository, never()).insertIfAbsent(anyLong(), any());
	}

	private void 저장된_행(int version) {
		UserRestriction row = 행(version);
		when(userRestrictionRepository.findById(7L)).thenReturn(Optional.of(row));
	}

	private static UserRestriction 행(int version) {
		UserRestriction row = mock(UserRestriction.class);
		when(row.getUserId()).thenReturn(7L);
		when(row.getVersion()).thenReturn(version);
		when(row.getUpdatedAt()).thenReturn(OffsetDateTime.parse("2026-10-30T12:00:00+09:00"));
		return row;
	}
}
