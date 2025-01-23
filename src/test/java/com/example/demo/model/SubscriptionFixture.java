package com.example.demo.model;

import java.util.UUID;

public abstract class SubscriptionFixture {

	public static Subscription hanSolo = 
			subscription(UUID.fromString("aa78baca-d552-4469-98b3-a4203d05fd0a"));
	
	public static Subscription subscription() {
		return subscription(UUID.randomUUID());
	}

	public static Subscription subscription(UUID uuid) {
		return new Subscription(uuid, "Han", "Solo", "han.solo@example.com");
	}

}
