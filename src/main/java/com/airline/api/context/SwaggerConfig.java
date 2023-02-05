package com.airline.api.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.airline.api.controllers"))
				.paths(PathSelectors.any())
				.build()
				.apiInfo(apiInfo());
	}

	public ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("Airline API")
				.description("Swagger Airline REST API Documentation")
				.version("1.0.0")
				.termsOfServiceUrl("https://example.com")
				.licenseUrl("https://example.com")
				.contact(new Contact("Sergio Escudero Manzano", "https://example.com", "sergio.escudero.dev@gmail.com"))
				.build();
	}
}
