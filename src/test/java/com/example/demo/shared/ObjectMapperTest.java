package com.example.demo.shared;

import static com.example.demo.model.SubscriptionFixture.subscription;
import static com.example.demo.model.SubscriptionFixture.withAuditFields;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Description;

import com.example.demo.model.Subscription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

@JsonTest
class ObjectMapperTest {
	
	Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	ObjectMapper objectMapper;

	@Test
	@Description("check auto config")	
	void checkAutoConfig() {
		// this should check application.yml spring.jackson configuration 
		DeserializationConfig deserializationConfig = objectMapper.getDeserializationConfig();
		assertThat(deserializationConfig
				.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
		assertThat(deserializationConfig
				.getDefaultPropertyInclusion().getValueInclusion()).isEqualTo(JsonInclude.Include.NON_NULL);
		
		assertThat(objectMapper.getRegisteredModuleIds()).contains("jackson-datatype-jsr310");
	}
	
	@Description("should deserialize correcty")
	@ParameterizedTest
	@MethodSource("samplesForSerializationDeserialization")
	void shouldDetserializeCorrectly(
			String json, Subscription expected, Optional<Class<? extends Exception>> optExpectedExceptionType) throws Exception {

		if (Objects.nonNull(expected)) {
			Subscription actual = objectMapper.readValue(json, Subscription.class);
			assertThat(actual).isEqualTo(expected);
		}
		if (optExpectedExceptionType.isPresent()){
			Class<? extends Exception> type = optExpectedExceptionType.get();
			assertThatThrownBy(
					() -> objectMapper.readValue(json, Subscription.class)
			).isInstanceOf(type);	
		}
	}

	private static Stream<Arguments> samplesForSerializationDeserialization() {
		return Stream.of(
			// valid JSON
			Arguments.of("""
			{
				"id": "4e39b4a8-916f-4ddd-ac25-1b9b76d20442",
				"firstName": "Han",
				"lastName": "Solo",
				"email": "han.solo@example.com",
				"createdOn": "2025-01-26T08:51:06.581221355Z",
				"modifiedOn": "2025-01-26T09:01:06.581221355Z",
				"version": 4711
			}
			""",
			withAuditFields(subscription(UUID.fromString("4e39b4a8-916f-4ddd-ac25-1b9b76d20442")),
			Instant.parse("2025-01-26T08:51:06.581221355Z"), 
			Instant.parse("2025-01-26T09:01:06.581221355Z"), 
			4711),
			Optional.empty()),
			
			// valid JSON with unknown property 
			Arguments.of("""
			{"dummy": "value"}"
			""", emptySubscription(), Optional.empty()),
			
			// empty String 
			Arguments.of("", null, Optional.of(MismatchedInputException.class)),

			// non parseable JSON 
			Arguments.of("junk-JSON", null, Optional.of(JsonParseException.class))
		);
	}

	private static Subscription emptySubscription() {
		return new Subscription(null, null, null, null);
	}
}
