package com.ea.ocr.swaggerr.resource;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.builders.ApiInfoBuilder;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.ea.ocr.controller")).paths(regex("/oa.*")).build()
				.apiInfo(metaData());
	}

	private ApiInfo metaData() {
		return new ApiInfoBuilder().title("EA-OCR Endpoints")
				.description("EA-OCR Endpoints").version("2.0").build();
	}

}