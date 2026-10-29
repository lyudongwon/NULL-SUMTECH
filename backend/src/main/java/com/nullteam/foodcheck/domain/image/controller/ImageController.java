package com.nullteam.foodcheck.domain.image.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nullteam.foodcheck.domain.image.dto.ImageUploadResponse;
import com.nullteam.foodcheck.domain.image.service.ImageService;

/** REQ-1 사진 업로드 (docs/api.md). */
@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

	private final ImageService imageService;

	public ImageController(ImageService imageService) {
		this.imageService = imageService;
	}

	/** file이 없거나 multipart가 아니면 null로 받아 서비스에서 400을 낸다. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ImageUploadResponse upload(@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestParam(value = "source", required = false) String source) {
		return imageService.upload(file, source);
	}
}
