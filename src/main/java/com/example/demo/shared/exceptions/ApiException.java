package com.example.demo.shared.exceptions;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	private HttpStatus status;
	private URI type;
	private String title;
	private String detail;
	private Map<String, Object> properties;

	public ApiException(HttpStatus status, URI type, String title, String detail, Map<String, Object> properties) {
		super(detail);
		this.status = status;
		this.type = type;
		this.title = title;
		this.detail = detail;
		this.properties = properties;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public URI getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getDetail() {
		return detail;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

}
