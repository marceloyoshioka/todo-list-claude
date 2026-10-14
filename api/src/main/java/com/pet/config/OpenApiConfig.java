package com.pet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI todoListOpenApi() {
		return new OpenAPI().info(new Info()
				.title("TODO List Didático")
				.description("API de estudo para praticar Spring Boot, JPA e boas práticas de REST.")
				.version("v1"));
	}

}
