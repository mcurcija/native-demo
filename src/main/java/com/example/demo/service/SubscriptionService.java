// src/main/java/com/example/demo/service/SubscriptionService.java
package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.model.Subscription;
import com.example.demo.persistence.SubscriptionRepository;
import com.example.demo.shared.exceptions.DuplicateSupbsciptionException;
import com.example.demo.shared.exceptions.SubscriptionNotFoundException;

@Service
public class SubscriptionService {
	
	private final SubscriptionRepository subscriptionRepository;

	public SubscriptionService(SubscriptionRepository subscriptionRepository) {
		this.subscriptionRepository = subscriptionRepository;
	}

	public List<Subscription> getAllSubscriptions() {
		return subscriptionRepository.findAll();
	}

	public Subscription getSubscription(UUID id) {
		Optional<Subscription> optional = subscriptionRepository.findById(id);
		return optional.orElseThrow(() -> new SubscriptionNotFoundException(id));
	}

	public Subscription createSubscription(Subscription subscription) {
		UUID id = subscription.id();
		if (subscriptionRepository.existsById(id)) {
			throw new DuplicateSupbsciptionException(id);
		}
		return subscriptionRepository.save(subscription);
	}

	public void deleteSubscription(UUID id) {
		if (! subscriptionRepository.existsById(id)) {
			throw new SubscriptionNotFoundException(id);
		}
		subscriptionRepository.deleteById(id);
	}

	public Subscription updateSubscription(UUID id, Subscription subscription) {
		Subscription existing = getSubscription(id);
		Subscription updated = existing.updateWith(
				subscription.firstName(), 
				subscription.lastName(),
				subscription.email());
		return subscriptionRepository.save(updated);
	}
}