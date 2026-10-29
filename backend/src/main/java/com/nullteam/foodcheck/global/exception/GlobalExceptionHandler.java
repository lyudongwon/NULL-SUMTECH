package com.nullteam.foodcheck.global.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 모든 에러를 api.md 공통 형식 { "detail": "<한국어 문장>" }으로 돌려준다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, String>> handleApi(ApiException e) {
		return detail(e.getStatus(), e.getMessage());
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentNotValidException.class,
			MethodArgumentTypeMismatchException.class,
	})
	public ResponseEntity<Map<String, String>> handleBadRequest(Exception e) {
		return detail(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException e) {
		return detail(HttpStatus.BAD_REQUEST, e.getParameterName() + " 값이 필요합니다.");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> handleTooLarge(MaxUploadSizeExceededException e) {
		return detail(HttpStatus.CONTENT_TOO_LARGE, "사진 용량이 너무 큽니다. (최대 10MB)");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(NoResourceFoundException e) {
		return detail(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleUnknown(Exception e) {
		log.error("처리하지 못한 오류", e);
		return detail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
	}

	private static ResponseEntity<Map<String, String>> detail(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of("detail", message));
	}
}
