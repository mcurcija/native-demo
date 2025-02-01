package com.example.demo.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.demo.web.PingEndpoint.PingResponse;

@WebMvcTest(PingEndpoint.class)
class PingEndpointTest {

	@Autowired
	WebTestClient webTestClient;

	@Test
	void shouldRespondToPingCorrectly() {
		PingResponse actual = webTestClient
				.get()
				.uri("/ping")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectBody(PingResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(actual.status()).isEqualTo("UP");
	}

}
