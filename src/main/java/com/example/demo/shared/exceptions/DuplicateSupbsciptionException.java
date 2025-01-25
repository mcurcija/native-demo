package com.example.demo.shared.exceptions;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class DuplicateSupbsciptionException extends ApiException {

	public static final String DETAIL_TEMPLATE = "Subscription with id: %s already exists";

	public DuplicateSupbsciptionException(UUID id) {
		super(
			HttpStatus.CONFLICT, 
			URI.create("urn:duplicate-subscription"), 
			"Duplicate subscription", 
			DETAIL_TEMPLATE.formatted(id),
			null);
	}
}
