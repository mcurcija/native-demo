package com.example.demo.web;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.model.Subscription;
import com.example.demo.persistence.SubscriptionRepository;
import com.example.demo.shared.exceptions.DuplicateSupbsciptionException;
import com.example.demo.shared.exceptions.SubscriptionNotFoundException;

@RestController
public class SubscriptionEndpoints {

	@Autowired
	SubscriptionRepository subscriptionRepository;

	@GetMapping("subscriptions")
	public List<Subscription> getAll() {
		return subscriptionRepository.findAll();
	}

	@PutMapping("subscriptions")
	public ResponseEntity<Subscription> create(@RequestBody Subscription subscription) {
		if (subscriptionRepository.findById(subscription.id()).isPresent()) {
			throw new DuplicateSupbsciptionException(subscription.id());
		}
		Subscription saved = subscriptionRepository.save(subscription);
		URI location = ServletUriComponentsBuilder
			.fromCurrentRequest().path("/{id}")
			.buildAndExpand(saved.id()).toUri();
		
		return ResponseEntity.created(location).body(subscription);
	}

	@GetMapping("subscriptions/{id}")
	public Subscription get(@PathVariable UUID id) {
		Optional<Subscription> optional = subscriptionRepository.findById(id);
		return optional.orElseThrow(() -> new SubscriptionNotFoundException(id));
	}

	@PutMapping("subscriptions/{id}")
	public Subscription replace(@PathVariable UUID id,
			@RequestBody Subscription subscription) {
		return subscriptionRepository.save(subscription);
	}

	@DeleteMapping("subscriptions/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") UUID uuid) {
		subscriptionRepository.deleteById(uuid);
		return ResponseEntity.noContent().build();
	}

}
