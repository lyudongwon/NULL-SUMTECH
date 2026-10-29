package com.nullteam.foodcheck.global.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 모든 에러를 api.md 공통 형식 { "detail": "<영어 문장>" }으로 돌려준다. */
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
		return detail(HttpStatus.BAD_REQUEST, "The request is not in a valid format.");
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException e) {
		return detail(HttpStatus.BAD_REQUEST, e.getParameterName() + " is required.");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> handleTooLarge(MaxUploadSizeExceededException e) {
		return detail(HttpStatus.CONTENT_TOO_LARGE, "The photo is too large (max 10MB).");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(NoResourceFoundException e) {
		return detail(HttpStatus.NOT_FOUND, "Not found.");
	}

	/** 아래 Exception 처리에 잡혀 500이 되지 않도록 메서드·본문 형식 오류는 따로 받는다. */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, String>> handleMethod(HttpRequestMethodNotSupportedException e) {
		return detail(HttpStatus.METHOD_NOT_ALLOWED, "This method is not allowed.");
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<Map<String, String>> handleMediaType(HttpMediaTypeNotSupportedException e) {
		return detail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "This content type is not supported.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleUnknown(Exception e) {
		log.error("처리하지 못한 오류", e);
		return detail(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on the server.");
	}

	private static ResponseEntity<Map<String, String>> detail(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of("detail", message));
	}
}
