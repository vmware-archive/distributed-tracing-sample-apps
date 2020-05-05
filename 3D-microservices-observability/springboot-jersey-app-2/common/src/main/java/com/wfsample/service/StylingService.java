package com.wfsample.service;

import com.wfsample.common.dto.OrderDTO;
import com.wfsample.common.dto.ResponseDTO;
import com.wfsample.common.dto.ShirtStyleDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class StylingService {

  @Autowired
  @LoadBalanced
  protected RestTemplate restTemplate;
  public static final String STYLING_SERVICE_URL = "http://STYLING-SERVICE";
  protected String serviceUrl;

  public StylingService(String serviceUrl) {
    this.serviceUrl = serviceUrl.startsWith("http") ?
        serviceUrl : "http://" + serviceUrl;
  }

  public ResponseDTO getHello() {
    ResponseDTO response = restTemplate.getForObject(serviceUrl + "/hello", ResponseDTO.class);
    return response;
  }

  public List<ShirtStyleDTO> getAllStyles() throws Exception {
    List<ShirtStyleDTO> response = restTemplate.getForObject(serviceUrl + "/styles", new ArrayList<ShirtStyleDTO>().getClass());
    if(response == null || response.size() == 0) {
      throw new Exception("Failed to retrieve list of styles.");
    }
    return response;
  }

  public ResponseDTO makeShirts(String styleName, int quantity) throws Exception {
    String requestUrl = serviceUrl + "/shirts";
    OrderDTO order = new OrderDTO();
    order.setStyleName(styleName);
    order.setQuantity(quantity);
    ResponseDTO response = restTemplate.postForObject(requestUrl, order, ResponseDTO.class);
    if(!response.getStatus().equalsIgnoreCase("OK")) {
      throw new Exception(response.getStatus() + ":" + response.getMessage());
    }
    return response;
  }
}
