package com.example.demo.web;

import static com.example.demo.model.SubscriptionFixture.hanSolo;
import static com.example.demo.model.SubscriptionFixture.subscription;
import static com.example.demo.model.SubscriptionFixture.withAuditFields;
import static com.example.demo.shared.exceptions.ProblemDetailUtils.problemDetailFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.example.demo.model.Subscription;
import com.example.demo.persistence.SubscriptionRepository;
import com.example.demo.service.SubscriptionService;
import com.example.demo.shared.conf.SecurityConfig;
import com.example.demo.shared.exceptions.DuplicateSupbsciptionException;
import com.example.demo.shared.exceptions.SubscriptionNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(SubscriptionEndpoints.class)
@DisabledInAotMode
@Import({ SubscriptionService.class, SecurityConfig.class, TokenService.class })
class SubscriptionEndpointsTest {

	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	WebTestClient webTestClient;

	@Autowired
	SubscriptionService subscriptionService;

	@MockitoBean
	SubscriptionRepository subscriptionRepository;

	@Captor
	ArgumentCaptor<Subscription> subscriptionCaptor;

	@Autowired
	TokenService tokenService;
	
	@Value("spring.security.user.name")
	String username;

	@Value("spring.security.user.password")
	String password;
	
	@Test
	@DisplayName("should get single subscription")
	void shouldGetSingleCorrectly() {
		when(subscriptionRepository.findById(hanSolo.id())).thenReturn(Optional.of(hanSolo));
		
		var actual = webTestClient
				.get().uri("/subscriptions/{id}", hanSolo.id())
				.accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
				.exchange()
				.expectStatus().isOk().expectBody(Subscription.class)
				.returnResult().getResponseBody();

		assertThat(actual).usingRecursiveComparison().isEqualTo(hanSolo);
	}

	@Test
	@DisplayName("should get all subscriptions")
	void shouldGetAllCorrectly() {
		// mock find all
		when(subscriptionRepository.findAll()).thenReturn(List.of(subscription()));

		var actual = webTestClient
				.get().uri("/subscriptions")
				.accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
				.exchange()
				.expectStatus().isOk().expectBodyList(Subscription.class)
				.returnResult().getResponseBody();

		assertThat(actual).hasSize(1);
	}

	@Test
	@DisplayName("should post subscription")	
	void shouldPostSubscriptionCorrectly() {
		Subscription newSubscription = subscription();

		when(subscriptionRepository.existsById(newSubscription.id())).thenReturn(false);
		when(subscriptionRepository.save(subscriptionCaptor.capture())).thenReturn(newSubscription);

		Subscription actual = webTestClient
				.post().uri("/subscriptions")
				.contentType(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
				.accept(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(newSubscription))
				.exchange()
				.expectHeader().location("http://localhost/subscriptions/%s".formatted(newSubscription.id()))
				.expectStatus().isCreated().expectBody(Subscription.class)
				.returnResult().getResponseBody();

		assertThat(actual).usingRecursiveComparison().isEqualTo(newSubscription);
	}


	@Test	
	@DisplayName("should delete subscription")
	void shouldDeleteSubscriptionCorrectly() {
		UUID id = UUID.randomUUID();
		
		when(subscriptionRepository.existsById(id)).thenReturn(true);
		
		webTestClient.delete().uri("/subscriptions/{id}", id)
				.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
				.exchange()
				.expectStatus()
				.isNoContent();
		verify(subscriptionRepository, times(1)).deleteById(id);
	}

	@Test
	@DisplayName("should throw subscription not foundon when deleting inexistent")	
	void shouldThrowSubscriptionNotFoundOnDeleteInexistentSubscription() {
		UUID id = UUID.randomUUID();
		
		when(subscriptionRepository.existsById(id)).thenReturn(false);
		
		SubscriptionNotFoundException notFoundException = new SubscriptionNotFoundException(id);
		ProblemDetail expected = problemDetailFor(notFoundException, "/subscriptions/%s".formatted(id));

		var actual = webTestClient.delete().uri("/subscriptions/{id}", id)
				.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody(ProblemDetail.class)
				.returnResult().getResponseBody();

		assertThat(actual)
			.usingRecursiveComparison()
			.ignoringFields("properties")
			.isEqualTo(expected);
		
		verify(subscriptionRepository, never()).deleteById(id);
	}

	@Test
	@DisplayName("should result with duplicate subscription exception on create")
	void shouldResultWithDuplicateSubscriptionExceptionOnCreate() {
		Subscription newSubscription = subscription();
		UUID id = newSubscription.id();
		
		when(subscriptionRepository.existsById(newSubscription.id())).thenReturn(true);

		DuplicateSupbsciptionException duplicateSubscriptionException = new DuplicateSupbsciptionException(id);
		ProblemDetail expected = problemDetailFor(duplicateSubscriptionException, "/subscriptions");
		
		ProblemDetail actual = webTestClient
				.post().uri("/subscriptions")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
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

		ProblemDetail expected = problemDetailFor(new SubscriptionNotFoundException(inexistentId), "/subscriptions/%s".formatted(inexistentId));
		
		ProblemDetail actual = webTestClient
				.get().uri("/subscriptions/{id}", inexistentId)
				.accept(MediaType.APPLICATION_JSON)
				.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody(ProblemDetail.class)
				.returnResult().getResponseBody();

		assertThat(actual)
			.usingRecursiveComparison()
			.ignoringFields("properties")
			.isEqualTo(expected);
	}
	
	@Test
	@DisplayName("should serialize instant correctly")
	void shouldSerializeInstantCorrectly() {
		Subscription subscription = withAuditFields(hanSolo, 
				Instant.parse("2025-01-26T08:51:06.581221355Z"), null, null);
		
		when(subscriptionRepository.findById(hanSolo.id())).thenReturn(Optional.of(subscription));
		
		webTestClient
			.get().uri("/subscriptions/{id}", hanSolo.id())
			.accept(MediaType.APPLICATION_JSON)
			.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			// expected due to 'spring.jackson.default-property-inclusion: non-null' 
			.jsonPath("$.modifiedOn").doesNotHaveJsonPath()
			// expected due to 'spring.jackson.serialization.write-dates-as-timestamps: true'
			.jsonPath("$.createdOn").isEqualTo("2025-01-26T08:51:06.581221355Z");
	}

	@Test
	@DisplayName("should check hat 'fail-on-unknown-properties: false' correct")
	void shouldNotFailOnUnknownProperties() {
		Subscription newSubscription = subscription();
		when(subscriptionRepository.existsById(newSubscription.id())).thenReturn(false);
		when(subscriptionRepository.save(subscriptionCaptor.capture())).thenReturn(newSubscription);

		// body with unknown properties
		String jsonBody = """
		{
			"id": "4e39b4a8-916f-4ddd-ac25-1b9b76d20442",
			"firstName": "Han",
			"lastName": "Solo",
			"email": "han.solo@example.com",
			"dummy": "value"
		}
		""";

		webTestClient
			.post().uri("/subscriptions")
			.accept(MediaType.APPLICATION_JSON)
			.header("authorization", "Bearer " + tokenService.token(configuredAuthentication()))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(jsonBody)
			.exchange()
			.expectStatus().isCreated();
	}

	private UsernamePasswordAuthenticationToken configuredAuthentication() {
		return new UsernamePasswordAuthenticationToken(username, password);
	}
	
}
