package com.example.demo.shared.conf;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.service.SubscriptionService;
import com.example.demo.web.APIConstants;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${jwt.public.key}")
	RSAPublicKey key;

	@Value("${jwt.private.key}")
	RSAPrivateKey priv;
	
	@Bean
	@Order(1)
	SecurityFilterChain unsecuredChain(HttpSecurity http) throws Exception {
		return http
			.csrf(withDefaults())
			.securityMatcher("/ping")
			.sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
			.authorizeHttpRequests(authorize ->
				authorize.anyRequest().permitAll()
			).build();
	}
	
	@Bean
	@Order(2)
	SecurityFilterChain apiChain(
			HttpSecurity http, 
			CustomAuthenticationExceptionEntryPoint customAuthenticationExceptionEntryPoint,
			CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {
		return http
			.csrf(withDefaults())
			.securityMatcher(APIConstants.PATH_EXT_API.concat("/**"))
			.authorizeHttpRequests(authorize -> 
				authorize.anyRequest().hasAuthority("SCOPE_%s".formatted(SubscriptionService.SUBSCRIPTION_ADMIN))
			)
			.oauth2ResourceServer(oauth2 -> oauth2
					.jwt(withDefaults())
					.authenticationEntryPoint(customAuthenticationExceptionEntryPoint)
					.accessDeniedHandler(customAccessDeniedHandler)
			)
			.sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
			.build();
	}
	
	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(this.key).build();
	}

	@Bean
	JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(this.key).privateKey(this.priv).build();
		JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwks);
	}	
	

}