package com.wfsample.service;

import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class DeliveryService {

  @Autowired
  @LoadBalanced
  protected RestTemplate restTemplate;
  public static final String DELIVERY_SERVICE_URL = "http://DELIVERY-SERVICE";
  protected String serviceUrl;

  public DeliveryService(String serviceUrl) {
    this.serviceUrl = serviceUrl.startsWith("http") ?
        serviceUrl : "http://" + serviceUrl;
  }

  public ResponseDTO getHello() {
    ResponseDTO response = restTemplate.getForObject(serviceUrl + "/hello", ResponseDTO.class);
    return response;
  }

  public ResponseDTO dispatch(String orderNum, PackedShirtsDTO packedShirtsDTO) {
    return restTemplate.postForObject(serviceUrl + "/dispatch/" + orderNum, packedShirtsDTO, ResponseDTO.class);
  }
}
