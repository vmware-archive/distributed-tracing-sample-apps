package com.wfsample.service;

import com.wfsample.common.dto.ResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ShoppingService {

  @Autowired
  @LoadBalanced
  protected RestTemplate restTemplate;
  public static final String STYLING_SERVICE_URL = "http://SHOPPING-SERVICE";
  protected String serviceUrl;

  public ShoppingService(String serviceUrl) {
    this.serviceUrl = serviceUrl.startsWith("http") ?
        serviceUrl : "http://" + serviceUrl;
  }

  public ResponseDTO getHello() {
    ResponseDTO response = restTemplate.getForObject(serviceUrl + "/hello", ResponseDTO.class);
    return response;
  }
}
