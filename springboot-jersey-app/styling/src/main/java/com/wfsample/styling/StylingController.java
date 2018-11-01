package com.wfsample.styling;

import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.service.DeliveryApi;
import com.wfsample.service.StylingApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import static java.util.stream.Collectors.toList;

/**
 * Driver for styling service which manages different styles of shirts and takes orders for a shirts
 * of a given style.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class StylingController implements StylingApi {
  private final DeliveryApi deliveryApi;
  private List<ShirtStyleDTO> shirtStyleDTOS;

  StylingController() {
    String deliveryUrl = "http://localhost:50052";
    this.deliveryApi = BeachShirtsUtils.createProxyClient(deliveryUrl, DeliveryApi.class);
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

  public List<ShirtStyleDTO> getAllStyles() {
    return this.shirtStyleDTOS;
  }

  public DeliveryStatusDTO makeShirts(String id, int quantity) {
    String orderNum = UUID.randomUUID().toString();
    List<ShirtDTO> packedShirts = new ArrayList<>();
    for (int i = 0; i < quantity; i++) {
      packedShirts.add(new ShirtDTO(new ShirtStyleDTO(id, id + "Image")));
    }
    PackedShirtsDTO packedShirtsDTO = new PackedShirtsDTO(packedShirts.stream().
        map(shirt -> new ShirtDTO(
            new ShirtStyleDTO(shirt.getStyle().getName(), shirt.getStyle().getImageUrl()))).
        collect(toList()));
    Response deliveryResponse = deliveryApi.dispatch(orderNum, packedShirtsDTO);
    return deliveryResponse.readEntity(DeliveryStatusDTO.class);
  }
}
