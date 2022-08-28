package com.kmarinos.springrapidrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringRapidRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRapidRestApplication.class, args);
	}

}
