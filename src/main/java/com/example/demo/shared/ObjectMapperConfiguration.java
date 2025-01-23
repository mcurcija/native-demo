package com.example.demo.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ObjectMapperConfiguration {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Bean
	ObjectMapper objectMapper(@Autowired Jackson2ObjectMapperBuilder builder) {
		ObjectMapper mapper = builder.build();
		logger.info("created ObjectMapper instance {}", mapper);
		return mapper;
	}

	@Bean
	Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder().serializationInclusion(Include.NON_NULL)
				.featuresToDisable(
	    				SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, 
						DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.modules(new JavaTimeModule());
		logger.info("created Jackson2ObjectMapperBuilder builder {}", builder);
		return builder;
	}
}
