package com.example.demo.shared.conf;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.demo.service.SubscriptionService;
import com.example.demo.web.PingEndpoint;
import com.example.demo.web.SubscriptionEndpoints;
import com.example.demo.web.TokenService;

@WebMvcTest({ PingEndpoint.class, SubscriptionEndpoints.class })
@Import({ SecurityConfig.class, TokenService.class })
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
	void shouldNotBeSeccured() {
		webTestClient.get().uri("/ping").exchange().expectStatus().isOk();
	}
	
	@Test
	void expectUnauthorizedWithoutToken() {
		webTestClient.get().uri("/subscriptions").exchange().expectStatus().isUnauthorized();
	}

	@Test
	void expectUnbauthorizedWithMalformedToken() {
		String token = "malformed-token"; 
		webTestClient
			.get().uri("/subscriptions")
			.header("Authorization", "Bearer " + token)
			.exchange().expectStatus().isUnauthorized();
	}
	
	@Test
	void expectOkWithValidToken() {
		Authentication authentication = configuredAuthentication();
		String token = tokenService.token(authentication);
		logger.info("token: {}", token);
		webTestClient.get().uri("/subscriptions")
			.header("Authorization", "Bearer " + token)
			.exchange().expectStatus().isOk();
	}

	private UsernamePasswordAuthenticationToken configuredAuthentication() {
		return new UsernamePasswordAuthenticationToken(username, password);
	}
}
