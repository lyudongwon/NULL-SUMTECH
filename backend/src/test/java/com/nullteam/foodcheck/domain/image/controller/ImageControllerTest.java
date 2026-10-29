package com.nullteam.foodcheck.domain.image.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nullteam.foodcheck.domain.image.entity.Image;
import com.nullteam.foodcheck.domain.image.repository.ImageRepository;
import com.nullteam.foodcheck.domain.image.service.ImageBodyStore;
import com.nullteam.foodcheck.domain.image.service.ImageService;

/** REQ-1 사진 업로드의 상태 코드와 에러 문구(docs/api.md)를 HTTP로 확인한다. 서비스는 진짜, DB 저장소만 가짜다. */
@WebMvcTest(ImageController.class)
@Import({ ImageService.class, ImageBodyStore.class })
class ImageControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ImageRepository imageRepository;

	@Test
	void PNG_사진은_201과_image_id_크기를_돌려준다() throws Exception {
		when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));

		mvc.perform(multipart("/api/v1/images").file(file(png(40, 30), "image/png")).param("source", "camera"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.image_id").isNotEmpty())
				.andExpect(jsonPath("$.width").value(40))
				.andExpect(jsonPath("$.height").value(30));
	}

	@Test
	void 이미지가_아닌_파일은_415다() throws Exception {
		mvc.perform(multipart("/api/v1/images").file(file("not an image at all".getBytes(), "text/plain")))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.detail").value("This image format is not supported."));
		verify(imageRepository, never()).save(any());
	}

	@Test
	void 십MB를_넘으면_413이다() throws Exception {
		byte[] big = new byte[(int) ImageService.MAX_BYTES + 1];

		mvc.perform(multipart("/api/v1/images").file(file(big, "image/jpeg")))
				.andExpect(status().isContentTooLarge())
				.andExpect(jsonPath("$.detail").value("The photo is too large (max 10MB)."));
		verify(imageRepository, never()).save(any());
	}

	@Test
	void file이_없으면_400이다() throws Exception {
		mvc.perform(multipart("/api/v1/images"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Please choose a photo to upload."));
	}

	@Test
	void 잘못된_source_값은_400이다() throws Exception {
		mvc.perform(multipart("/api/v1/images").file(file(png(4, 4), "image/png")).param("source", "scanner"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Invalid source value."));
		verify(imageRepository, never()).save(any());
	}

	private static MockMultipartFile file(byte[] bytes, String contentType) {
		return new MockMultipartFile("file", "menu", contentType, bytes);
	}

	private static byte[] png(int width, int height) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", out);
		return out.toByteArray();
	}
}
