package com.wavefront.styling;

import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ResponseDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.common.dto.ShirtDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
@ComponentScan(basePackages="com.wfsample.bean")
public class StylingController {

  @Autowired
  DeliveryService deliveryService;

  protected Logger logger = Logger.getLogger(StylingController.class.getName());
  private List<ShirtStyleDTO> shirtStyleDTOS;

  @PostConstruct
  public void init() {
    shirtStyleDTOS = new ArrayList<>();
    ShirtStyleDTO dto = new ShirtStyleDTO();
    dto.setName("style1");
    dto.setImageUrl("style1Image");
    ShirtStyleDTO dto2 = new ShirtStyleDTO();
    dto2.setName("style2");
    dto2.setImageUrl("style2Image");
    shirtStyleDTOS.add(dto);
    shirtStyleDTOS.add(dto2);
  }

  @GetMapping(path="/styles")
  public List<ShirtStyleDTO> getAllStyles() {
    return this.shirtStyleDTOS;
  }

  @GetMapping(path="/hello")
  public ResponseDTO hello() {
    return ResponseDTO.ok().msg("hello from Styling Service").build();
  }

  @PostMapping(path="/shirts")
  public ResponseDTO makeShirts(@RequestBody OrderDTO orderDTO) throws Exception {

    String styleName = orderDTO.getStyleName();
    int quantity = orderDTO.getQuantity();

    if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
      String msg = "Failed to make shirts!";
      logger.warning(msg);
      return ResponseDTO.status("SERVICE UNAVAILABLE").msg(msg).build();
    }
    String orderNum = UUID.randomUUID().toString();
    List<ShirtDTO> packedShirts = new ArrayList<ShirtDTO>();
    for (int i = 0; i < quantity; i++) {
      packedShirts.add(new ShirtDTO(new ShirtStyleDTO(styleName, styleName + "Image")));
    }
    PackedShirtsDTO packedShirtsDTO = new PackedShirtsDTO(packedShirts.stream().
        map(shirt -> new ShirtDTO(
            new ShirtStyleDTO(shirt.getStyle().getName(), shirt.getStyle().getImageUrl()))).collect(toList()));
    ResponseDTO deliveryResponse = deliveryService.dispatch(orderNum, packedShirtsDTO);
    if(deliveryResponse.getStatus().equalsIgnoreCase("OK")) {
      return deliveryResponse;
    } else {
      String msg = "Failed to make shirts!";
      logger.warning(msg);
      return ResponseDTO.status("FAILED").msg(msg).build();
    }
  }
}
