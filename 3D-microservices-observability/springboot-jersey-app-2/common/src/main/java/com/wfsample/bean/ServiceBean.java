package com.wfsample.bean;
import com.wfsample.service.*;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ServiceBean {

  @LoadBalanced    // Make sure to create the load-balanced template
  @Bean
  RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public DeliveryService getDeliveryServiceBean() {
    return new DeliveryService(DeliveryService.DELIVERY_SERVICE_URL);
  }

  @Bean
  public StylingService getStylingServiceBean() {
    return new StylingService(StylingService.STYLING_SERVICE_URL);
  }
}
