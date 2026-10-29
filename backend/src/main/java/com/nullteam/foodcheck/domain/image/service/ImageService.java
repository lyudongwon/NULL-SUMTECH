package com.nullteam.foodcheck.domain.image.service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nullteam.foodcheck.domain.image.dto.ImageSource;
import com.nullteam.foodcheck.domain.image.dto.ImageUploadResponse;
import com.nullteam.foodcheck.domain.image.entity.Image;
import com.nullteam.foodcheck.domain.image.repository.ImageRepository;
import com.nullteam.foodcheck.global.exception.ApiException;

/** REQ-1 사진 업로드. 브라우저가 OPT-3으로 줄인 사진을 받아 image_id를 발급한다. */
@Service
public class ImageService {

	public static final long MAX_BYTES = 10L * 1024 * 1024;
	public static final String TOO_LARGE = "사진 용량이 너무 큽니다. (최대 10MB)";

	private static final SecureRandom RANDOM = new SecureRandom();

	private final ImageRepository imageRepository;
	private final ImageBodyStore bodyStore;

	public ImageService(ImageRepository imageRepository, ImageBodyStore bodyStore) {
		this.imageRepository = imageRepository;
		this.bodyStore = bodyStore;
	}

	@Transactional
	public ImageUploadResponse upload(MultipartFile file, String source) {
		if (file == null || file.isEmpty()) {
			throw ApiException.badRequest("업로드할 사진이 없습니다.");
		}
		if (file.getSize() > MAX_BYTES) {
			throw new ApiException(HttpStatus.CONTENT_TOO_LARGE, TOO_LARGE);
		}
		ImageSource imageSource = parseSource(source);
		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			throw new IllegalStateException("업로드한 사진을 읽지 못했습니다.", e);
		}
		ImageFormat.Info info = ImageFormat.detect(bytes)
				.orElseThrow(() -> new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 이미지 형식입니다."));

		Image image = imageRepository.save(Image.create(newId(), info.width(), info.height(),
				imageSource == null ? null : imageSource.name(), info.mimeType()));
		bodyStore.put(image.getId(), bytes, info.mimeType());
		return new ImageUploadResponse(image.getId(), image.getWidth(), image.getHeight(), image.getUploadedAt());
	}

	/** 다른 도메인(menu)이 판독할 사진을 받는 곳. 없거나 본문이 지워졌으면 404를 던진다. */
	@Transactional(readOnly = true)
	public ImageBodyStore.Body loadBody(String imageId) {
		if (imageId == null || !imageRepository.existsById(imageId)) {
			throw notFound();
		}
		return bodyStore.find(imageId).orElseThrow(ImageService::notFound);
	}

	private static ApiException notFound() {
		return ApiException.notFound("이미지를 찾을 수 없습니다.");
	}

	private static ImageSource parseSource(String source) {
		if (source == null || source.isBlank()) {
			return null;
		}
		try {
			return ImageSource.valueOf(source.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw ApiException.badRequest("source 값이 올바르지 않습니다.");
		}
	}

	private static String newId() {
		byte[] random = new byte[6];
		RANDOM.nextBytes(random);
		return "img_" + HexFormat.of().formatHex(random);
	}
}
