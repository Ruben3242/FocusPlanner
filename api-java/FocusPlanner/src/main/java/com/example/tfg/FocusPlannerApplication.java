package com.example.tfg;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FocusPlannerApplication {

	@Value("${spring.profiles.active}")
	private String activeProfile;

	public static void main(String[] args) {
		SpringApplication.run(FocusPlannerApplication.class, args);
	}

	@PostConstruct
	public void logActiveProfile() {
		System.out.println("***Active Spring Profile: " + activeProfile + " ***");
	}
}