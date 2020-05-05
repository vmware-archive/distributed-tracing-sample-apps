package com.wavefront.shopping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wfsample.common.dto.OrderDTO;
import com.wfsample.common.dto.ResponseDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.service.StylingService;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages="com.wfsample.bean")
public class ShoppingController {

  protected Logger logger = Logger.getLogger(ShoppingController.class.getName());

  @Autowired
  StylingService stylingService;

  @GetMapping(path="/hello")
  public ResponseDTO hello() {
    return ResponseDTO.ok().msg("hello from Shopping Service").build();
  }

  @GetMapping(path="/menu")
  public List<ShirtStyleDTO> getMenu() {
    List<ShirtStyleDTO> styles = stylingService.getAllStyles();
    return styles;
  }

  @PostMapping(path="/order")
  public ResponseDTO orderShirts(@RequestBody OrderDTO orderDTO) {

    if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
      String msg = "Failed to order shirts!";
      logger.warning(msg);
      return ResponseDTO.status("SERVICE UNAVAILABLE").msg(msg).build();
    }
    ResponseDTO stylingResponse = stylingService.makeShirts(orderDTO.getStyleName(), orderDTO.getQuantity());
    if(stylingResponse.getStatus().equalsIgnoreCase("OK")) {
      return stylingResponse;
    } else {
      String msg = "Failed to order shirts!";
      logger.warning(msg);
      return ResponseDTO.status("FAILED").msg(msg).build();
    }
  }
}
