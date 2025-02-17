package com.example.demo.shared;

import static java.util.Objects.nonNull;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponse.Builder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.example.demo.shared.exceptions.ApiException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(ApiException.class)
	ErrorResponse handleAPIException(ApiException e) {
		Builder builder = ErrorResponse.builder(e, e.getStatus(), e.getDetail()).title(e.getTitle()).type(e.getType());
		if (nonNull(e.getProperties())) {
			e.getProperties().entrySet().forEach(entry -> builder.property(entry.getKey(), entry.getValue()));
		}
		builder.property("timestamp", Instant.now());
		return builder.build();
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	ProblemDetail handleAuthorizationDeniedException(AuthorizationDeniedException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
	}
	
	@ExceptionHandler(AuthenticationException.class)
	ProblemDetail handleAuthenticationException(AuthenticationException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
	}

}
