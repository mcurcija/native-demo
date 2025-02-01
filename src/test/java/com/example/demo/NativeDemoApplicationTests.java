package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.example.demo.shared.conf.TestMongoDBConfiguration;

@Import(TestMongoDBConfiguration.class)
@SpringBootTest
class NativeDemoApplicationTests {

	@Test
	void contextLoads() {
		// just loads context
	}

}
