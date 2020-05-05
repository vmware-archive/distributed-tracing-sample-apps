package com.wavefront.styling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StylingApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "styling-application");
		SpringApplication.run(StylingApplication.class, args);
	}

}
