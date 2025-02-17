package com.example.demo.shared.conf;

import static com.example.demo.service.SubscriptionService.SUBSCRIPTION_ADMIN;
import static com.example.demo.web.APIConstants.PATH_EXT_API;
import static com.example.demo.web.SubscriptionEndpoints.PATH_SUBSCRIPTIONS;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Flow.Subscription;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.demo.service.SubscriptionService;
import com.example.demo.web.PingEndpoint;
import com.example.demo.web.SubscriptionEndpoints;
import com.example.demo.web.TokenService;

@WebMvcTest({ PingEndpoint.class, SubscriptionEndpoints.class })
@Import({ 
	SecurityConfig.class, TokenService.class, 
	CustomAccessDeniedHandler.class, CustomAuthenticationExceptionEntryPoint.class
})
@ActiveProfiles("test")	
class SecurityConfigTest {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	WebTestClient webTestClient;
	
	@MockitoBean
	SubscriptionService subscriptionService;

	@Autowired
	TokenService tokenService;
	
	@Value("spring.security.user.name")
	String username;

	@Value("spring.security.user.password")
	String password;
	
	@Test
	@DisplayName("should not be secured")	
	void shouldNotBeSecured() {
		webTestClient.get().uri("/ping").exchange().expectStatus().isOk();
	}
	
	@Test
	void expectOkWithValidToken() {
		Authentication authentication = configuredAuthentication(SUBSCRIPTION_ADMIN);
		List<String> granted = authentication.getAuthorities().stream().map(auth -> auth.getAuthority()).toList();
		assertThat(granted).contains(SUBSCRIPTION_ADMIN);

		String token = tokenService.token(authentication);
		logger.info("token: {}", token);
		List<Subscription> subscriptions = clientExtApi().get().uri(PATH_SUBSCRIPTIONS)
			.header("Authorization", "Bearer " + token)
			.exchange().expectStatus().isOk()
			.expectBodyList(Subscription.class).returnResult().getResponseBody();
		
		assertThat(subscriptions).usingRecursiveAssertion().isEqualTo(subscriptions);
	}

	@Test
	void expectUnauthorizedWithoutToken() {
		ProblemDetail expected = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
				"Full authentication is required to access this resource");
		expected.setInstance(URI.create("/ext/api/1.0/subscriptions"));

		ProblemDetail actual = clientExtApi().get().uri(PATH_SUBSCRIPTIONS)
				.exchange().expectStatus().isUnauthorized()
				.expectBody(ProblemDetail.class).returnResult().getResponseBody();
		
		assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
	}

	@Test
	void expectUnbauthorizedWithMalformedToken() {
		String token = "malformed-token"; 
		ProblemDetail actual =  clientExtApi().get().uri(PATH_SUBSCRIPTIONS)
			.header("Authorization", "Bearer " + token)
			.exchange().expectStatus().isUnauthorized()
			.expectBody(ProblemDetail.class).returnResult().getResponseBody();
		
		assertThat(actual.getDetail()).isEqualTo("An error occurred while attempting to decode the Jwt: Malformed token");
	}
	
	@Test
	void expectForbiddenWithValidTokenNotHavingPermission() {
		Authentication authentication = configuredAuthentication();
		
		List<String> granted = authentication.getAuthorities()
				.stream().map(auth -> auth.getAuthority()).toList();
		assertThat(granted).isEmpty();
		
		String token = tokenService.token(authentication);
		var problemDetail = clientExtApi().get().uri(PATH_SUBSCRIPTIONS)
			.header("Authorization", "Bearer " + token)
			.headers(headers -> headers.setAcceptLanguageAsLocales(List.of(Locale.GERMAN)))
			.exchange().expectStatus().isForbidden()
			.expectBody(ProblemDetail.class)
			.returnResult().getResponseBody();
		
		assertThat(problemDetail.getDetail()).isEqualTo("Access Denied");
	}
	
	private Authentication configuredAuthentication(String... authorities ) {
		return new TestingAuthenticationToken("user", null, authorities);
	}
	
	private WebTestClient clientExtApi() {
		return webTestClient.mutate().baseUrl(PATH_EXT_API).build();
	}
	
}
