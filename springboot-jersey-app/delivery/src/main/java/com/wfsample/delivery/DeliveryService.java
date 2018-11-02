package com.wfsample.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Springboot class for Delivery Service.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@SpringBootApplication
@EnableScheduling
public class DeliveryService {

  public static void main(String[] args) {
    SpringApplication.run(DeliveryService.class, args);
  }

}
