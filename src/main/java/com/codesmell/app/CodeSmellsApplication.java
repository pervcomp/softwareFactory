package com.codesmell.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CodeSmellsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeSmellsApplication.class, args);
	}
}
