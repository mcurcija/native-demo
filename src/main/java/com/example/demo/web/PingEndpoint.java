package com.example.demo.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class PingEndpoint {

	@GetMapping("ping")
	public PingResponse ping() {
		return new PingResponse("UP");
	}
	
	public record PingResponse(String status) {}
	
	
}
