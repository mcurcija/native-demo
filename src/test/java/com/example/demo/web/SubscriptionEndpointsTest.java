package com.example.demo.web;

import static com.example.demo.model.SubscriptionFixture.hanSolo;
import static com.example.demo.model.SubscriptionFixture.subscription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.example.demo.model.Subscription;
import com.example.demo.persistence.SubscriptionRepository;
import com.example.demo.shared.exceptions.DuplicateSupbsciptionException;
import com.example.demo.shared.exceptions.ProblemDetailUtils;
import com.example.demo.shared.exceptions.SubscriptionNotFoundException;

@WebMvcTest(SubscriptionEndpoints.class)
@DisabledInAotMode
class SubscriptionEndpointsTest {

	@Autowired
	WebTestClient webTestClient;

	@MockitoBean
	SubscriptionRepository subscriptionRepository;

	@Captor
	ArgumentCaptor<Subscription> subscriptionCaptor;

	@Test
	void shouldGetSingleCorrectly() {
		when(subscriptionRepository.findById(hanSolo.id())).thenReturn(java.util.Optional.of(hanSolo));

		var actual = webTestClient
				.get().uri("/subscriptions/{id}", hanSolo.id())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk().expectBody(Subscription.class)
				.returnResult().getResponseBody();

		assertThat(actual).usingRecursiveComparison().isEqualTo(hanSolo);
	}

	@Test
	void shouldGetAllCorrectly() {
		// mock find all
		when(subscriptionRepository.findAll()).thenReturn(List.of(subscription()));

		var actual = webTestClient
				.get().uri("/subscriptions")
				.accept(MediaType.APPLICATION_JSON).exchange()
				.expectStatus().isOk().expectBodyList(Subscription.class)
				.returnResult().getResponseBody();

		assertThat(actual).isNotEmpty();
	}

	@Test
	void shouldPutSubscriptionCorrectly() {
		Subscription newSubscription = subscription();

		when(subscriptionRepository.findById(newSubscription.id())).thenReturn(Optional.empty());
		when(subscriptionRepository.save(subscriptionCaptor.capture())).thenReturn(newSubscription);

		Subscription actual = webTestClient
				.put().uri("/subscriptions")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(newSubscription))
				.exchange()
				.expectHeader().location("http://localhost/subscriptions/%s".formatted(newSubscription.id()))
				.expectStatus().isCreated().expectBody(Subscription.class)
				.returnResult().getResponseBody();

		assertThat(actual).usingRecursiveComparison().isEqualTo(newSubscription);
	}


	@Test	
	void shouldDeleteSubscriptionCorrectly() {
		UUID id = UUID.randomUUID();
		when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription(id)));
		webTestClient.delete().uri("/subscriptions/{id}", id)	
				.exchange()
				.expectStatus()
				.isNoContent();
	}	


	@Test
	void shouldResultWithDuplicateSubscriptionException() {
		Subscription newSubscription = subscription();

		when(subscriptionRepository.findById(newSubscription.id()))
		.thenReturn(Optional.of(newSubscription));

		UUID id = newSubscription.id();
		ProblemDetail expected = ProblemDetailUtils.problemDetailFor(new DuplicateSupbsciptionException(id), "/subscriptions");
		
		ProblemDetail actual = webTestClient
				.put().uri("/subscriptions")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(newSubscription))
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.CONFLICT.value())
				.expectBody(ProblemDetail.class)
				.returnResult().getResponseBody();

		assertThat(actual)
			.usingRecursiveComparison()
			.ignoringFields("properties")
			.isEqualTo(expected);
	}
	
	@Test
	void shouldResultWithSubscriptionNotFoundException() {
		UUID inexistentId = UUID.randomUUID();
		Subscription newSubscription = subscription(inexistentId);

		// return empty optional 
		when(subscriptionRepository.findById(newSubscription.id())).thenReturn(Optional.empty());

		ProblemDetail expected = ProblemDetailUtils.problemDetailFor(new SubscriptionNotFoundException(inexistentId), "/subscriptions/%s".formatted(inexistentId));
		
		ProblemDetail actual = webTestClient
				.get().uri("/subscriptions/{id}", inexistentId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.NOT_FOUND.value())
				.expectBody(ProblemDetail.class)
				.returnResult().getResponseBody();

		assertThat(actual)
			.usingRecursiveComparison()
			.ignoringFields("properties")
			.isEqualTo(expected);
	}
}
