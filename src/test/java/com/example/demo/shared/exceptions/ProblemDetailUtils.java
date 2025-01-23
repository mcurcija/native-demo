package com.example.demo.shared.exceptions;

import java.net.URI;

import org.springframework.http.ProblemDetail;

public abstract class ProblemDetailUtils {

	public static ProblemDetail problemDetailFor(ApiException ex, String instance) {
		ProblemDetail result = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getDetail());
		result.setType(ex.getType());
		result.setTitle(ex.getTitle());
		result.setProperties(ex.getProperties());
		result.setInstance(URI.create(instance));
		return result;
	}

}
