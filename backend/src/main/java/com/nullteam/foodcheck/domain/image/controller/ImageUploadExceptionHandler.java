package com.nullteam.foodcheck.domain.image.controller;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.nullteam.foodcheck.domain.image.service.ImageService;

/**
 * 업로드 한도(spring.servlet.multipart)를 넘으면 컨트롤러에 닿기 전에 예외가 나서 413으로 바꾼다(docs/api.md > REQ-1).
 * global의 Exception 처리(500)보다 먼저 잡히도록 순서를 앞에 둔다.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImageUploadExceptionHandler {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> handleTooLarge(MaxUploadSizeExceededException e) {
		return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(Map.of("detail", ImageService.TOO_LARGE));
	}
}
