package com.example.demo.shared.exceptions;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;

public class SubscriptionNotFoundException extends ApiException {
	
	private static final String DETAIL_TEMPLATE = "Subscription with id: %s does not exist";
	
	public SubscriptionNotFoundException(UUID id) {
		super(
			HttpStatus.NOT_FOUND, 
			URI.create("urn:subscription-not-found"), 
			"Subscription not found", 
			DETAIL_TEMPLATE.formatted(id), 
			null);
	}
	
}
