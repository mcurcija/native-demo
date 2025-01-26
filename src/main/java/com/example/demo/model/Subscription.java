package com.example.demo.model;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public record Subscription(
		@Id UUID id, 
		String firstName, 
		String lastName, 
		String email,
		Instant emailVerifiedOn,
		
		@CreatedDate Instant createdOn,
		@LastModifiedDate Instant modifiedOn,
		
		// NOTE ! version
		@Version Integer version
) {
	
	public Subscription(UUID id, String firstName, String lastName, String email) {
		this(id, firstName, lastName, email, null, null, null, null);
	}

	public Subscription updateWith(String firstName, String lastName, String email) {
		return new Subscription(
				id(), 
				firstName, 
				lastName, 
				email, 
				emailVerifiedOn(), 
				createdOn(), 
				modifiedOn(),
				version()
		);
	}
}
