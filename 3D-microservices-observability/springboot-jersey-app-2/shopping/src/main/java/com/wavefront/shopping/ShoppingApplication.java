package com.wavefront.shopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ShoppingApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "shopping-application");
		SpringApplication.run(ShoppingApplication.class, args);
	}

}
