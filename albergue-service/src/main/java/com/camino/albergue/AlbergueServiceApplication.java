package com.camino.albergue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AlbergueServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlbergueServiceApplication.class, args);
	}

}
