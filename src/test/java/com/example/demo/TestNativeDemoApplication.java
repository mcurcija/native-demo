package com.example.demo;

import org.springframework.boot.SpringApplication;

import com.example.demo.shared.conf.TestMongoDBConfiguration;

public class TestNativeDemoApplication {

	public static void main(String[] args) {
		SpringApplication.from(NativeDemoApplication::main).with(TestMongoDBConfiguration.class).run(args);
	}

}
