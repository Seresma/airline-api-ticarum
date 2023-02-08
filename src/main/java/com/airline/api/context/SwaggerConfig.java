package com.airline.api.context;

import com.airline.api.utils.Utils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

@EnableSwagger2
@Configuration
public class SwaggerConfig {
	private ApiKey apiKey() {
		return new ApiKey("JWT", "Authorization", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return List.of(new SecurityReference("JWT", authorizationScopes));
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.airline.api.controllers"))
				.paths(PathSelectors.any())
				.build()
				.securityContexts(Collections.singletonList(securityContext()))
				.securitySchemes(List.of(apiKey()))
				.apiInfo(apiInfo());
	}

	public ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title(Utils.capitalizeFirstLetter(GlobalConfig.AIRLINE_NAME) + " API")
				.description("Swagger " + Utils.capitalizeFirstLetter(GlobalConfig.AIRLINE_NAME) + " REST API Documentation")
				.version("1.0.0")
				.termsOfServiceUrl("https://example.com")
				.licenseUrl("https://example.com")
				.contact(new Contact("Sergio Escudero Manzano", "https://example.com", "sergio.escudero.dev@gmail.com"))
				.build();
	}
}
