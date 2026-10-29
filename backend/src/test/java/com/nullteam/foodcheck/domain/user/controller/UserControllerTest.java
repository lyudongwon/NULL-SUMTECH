package com.nullteam.foodcheck.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.nullteam.foodcheck.domain.user.entity.User;
import com.nullteam.foodcheck.domain.user.repository.UserRepository;
import com.nullteam.foodcheck.domain.user.service.UserService;

/**
 * REQ-4 사용자 번호 발급을 HTTP로 부른다. 서비스는 진짜를 쓰고, 저장소만 가짜로 둔다(DB의 Auto Increment 흉내).
 * 응답 필드 이름·상태 코드는 docs/api.md > REQ-4 사용자 번호 발급과 같아야 한다.
 */
@WebMvcTest(UserController.class)
@Import(UserService.class)
class UserControllerTest {

	private static final String URL = "/api/v1/users";

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private UserRepository userRepository;

	@BeforeEach
	void 저장하면_번호가_1부터_붙는다() {
		AtomicLong sequence = new AtomicLong();
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			ReflectionTestUtils.setField(user, "id", sequence.incrementAndGet());
			return user;
		});
	}

	@Test
	void 바디_없이_부르면_201과_snake_case_user_id_created_at을_돌려준다() throws Exception {
		mvc.perform(post(URL))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.user_id").value(1))
				.andExpect(jsonPath("$.created_at").isString())
				// camelCase 이름으로 새지 않는다
				.andExpect(jsonPath("$.userId").doesNotExist())
				.andExpect(jsonPath("$.createdAt").doesNotExist());
	}

	@Test
	void 부를_때마다_새_번호를_발급한다() throws Exception {
		mvc.perform(post(URL)).andExpect(jsonPath("$.user_id").value(1));
		mvc.perform(post(URL)).andExpect(jsonPath("$.user_id").value(2));

		verify(userRepository, times(2)).save(any(User.class));
	}

	@Test
	void GET은_405와_공통_에러_형식을_돌려준다() throws Exception {
		mvc.perform(get(URL))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.detail").value("This method is not allowed."));
	}
}
