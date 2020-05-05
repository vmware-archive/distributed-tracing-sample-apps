package com.wavefront.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DeliveryApplication {

	public static void main(String[] args) {
		System.setProperty("spring.config.name", "delivery-application");
		SpringApplication.run(DeliveryApplication.class, args);
	}

}
