package com.coursecraft.ai.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		Map<String, String> validationErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				validationErrors.put(error.getField(), error.getDefaultMessage()));

		ApiErrorResponse response = ApiErrorResponse.withValidationErrors(
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Request validation failed",
				request.getRequestURI(),
				validationErrors
		);

		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
			ConstraintViolationException exception,
			HttpServletRequest request
	) {
		Map<String, String> validationErrors = new LinkedHashMap<>();
		exception.getConstraintViolations().forEach(violation ->
				validationErrors.put(violation.getPropertyPath().toString(), violation.getMessage()));

		ApiErrorResponse response = ApiErrorResponse.withValidationErrors(
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Request validation failed",
				request.getRequestURI(),
				validationErrors
		);

		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MissingServletRequestParameterException.class,
			IllegalArgumentException.class
	})
	public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatus(
			ResponseStatusException exception,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
		String message = exception.getReason() != null ? exception.getReason() : status.getReasonPhrase();

		return buildResponse(status, message, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
		return buildResponse(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred",
				request
		);
	}

	private ResponseEntity<ApiErrorResponse> buildResponse(
			HttpStatus status,
			String message,
			HttpServletRequest request
	) {
		ApiErrorResponse response = ApiErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(response);
	}
}
