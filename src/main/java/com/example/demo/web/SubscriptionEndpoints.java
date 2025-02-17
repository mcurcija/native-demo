// src/main/java/com/example/demo/web/SubscriptionEndpoints.java
package com.example.demo.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.model.Subscription;
import com.example.demo.service.SubscriptionService;

@RestController
@RequestMapping(APIConstants.PATH_EXT_API)
public class SubscriptionEndpoints {

	public static final String PATH_SUBSCRIPTIONS = "/subscriptions";
	public static final String PATH_SINGLE_SUBSCRIPTION = "/subscriptions/{id}";
	
	@Autowired
	private SubscriptionService subscriptionService;

	@GetMapping(PATH_SUBSCRIPTIONS)
	public List<Subscription> getAllSubscriptions() {
		return subscriptionService.getAllSubscriptions();
	}

	@PostMapping(PATH_SUBSCRIPTIONS)
	public ResponseEntity<Subscription> createSubscription(@Validated @RequestBody Subscription subscription) {
		Subscription created = subscriptionService.createSubscription(subscription);
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping(PATH_SINGLE_SUBSCRIPTION)
	public Subscription getSubscription(@PathVariable UUID id) {
		return subscriptionService.getSubscription(id);
	}

	@PutMapping(PATH_SINGLE_SUBSCRIPTION)
	public Subscription updateSubscription(@PathVariable UUID id, @RequestBody Subscription subscription) {
		return subscriptionService.updateSubscription(id, subscription);
	}

	@DeleteMapping(PATH_SINGLE_SUBSCRIPTION)
	public ResponseEntity<Void> deleteSubscription(@PathVariable UUID id) {
		subscriptionService.deleteSubscription(id);
		return ResponseEntity.noContent().build();
	}
}