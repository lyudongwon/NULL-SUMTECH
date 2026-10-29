package com.nullteam.foodcheck.global.exception;

import org.springframework.http.HttpStatus;

/** api.md의 에러 응답 { "detail": "..." }으로 바뀌는 예외. 메시지는 사용자에게 보여 줄 한국어 문장이다. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String detail) {
		super(detail);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public static ApiException badRequest(String detail) {
		return new ApiException(HttpStatus.BAD_REQUEST, detail);
	}

	public static ApiException notFound(String detail) {
		return new ApiException(HttpStatus.NOT_FOUND, detail);
	}

	public static ApiException conflict(String detail) {
		return new ApiException(HttpStatus.CONFLICT, detail);
	}
}
