package com.example.demo.shared.exceptions;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

	private static final long serialVersionUID = -1008494027525850987L;
	private final HttpStatus status;
	private final URI type;
	private final String title;
	private final String detail;
	private final Map<String, Object> properties;

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
