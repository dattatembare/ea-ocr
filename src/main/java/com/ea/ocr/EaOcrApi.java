package com.ea.ocr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages={"com.ea.ocr"})// same as @Configuration @EnableAutoConfiguration @ComponentScan combined
public class EaOcrApi {

	public static void main(String[] args) {
		SpringApplication.run(EaOcrApi.class, args);
	}
}
