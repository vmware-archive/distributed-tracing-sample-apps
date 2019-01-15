package com.wfsample.styling;

import com.wfsample.common.B3HeadersRequestFilter;
import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.service.DeliveryApi;
import com.wfsample.service.StylingApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.core.HttpHeaders;
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
  private B3HeadersRequestFilter b3Filter;
  private static Logger logger = LoggerFactory.getLogger(StylingService.class);

  StylingController() {
//    String deliveryUrl = "http://localhost:50052";
    // Modify to use the service alias for delivery service in Kubernetes.
    String deliveryUrl = "http://deliveryservice:50052";
//    WavefrontJaxrsClientFilter wavefrontJaxrsFilter = null;
    /**
     * TODO: Initialize WavefrontJaxrsClientFilter in JerseyConfig.java, add an argument of it in
     * the constructor as well and uncomment the following line to use it.
     */
    // wavefrontJaxrsFilter = wfJaxrsClientFilter;
    // Filter to propagate B3 headers.
    b3Filter = new B3HeadersRequestFilter();
    this.deliveryApi = BeachShirtsUtils.createProxyClient(deliveryUrl, DeliveryApi.class, b3Filter);
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

  public List<ShirtStyleDTO> getAllStyles(HttpHeaders httpHeaders) {
    // Propagate B3 headers.
    b3Filter.setB3Headers(httpHeaders);
    return this.shirtStyleDTOS;
  }

  public Response makeShirts(String id, int quantity, HttpHeaders httpHeaders) {
    /*
     * TODO: Try to report the value of quantity using WavefrontHistogram.
     *
     * Viewing the quantity requested by various clients as a minute distribution and then applying
     * statistical functions (median, mean, min, max, p95, p99 etc.) on that data is really useful
     * to understand the user trend.
     */
    if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
      String msg = "Failed to make shirts!";
      logger.warn(msg);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();
    }      // Propagate B3 headers.
    b3Filter.setB3Headers(httpHeaders);
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
    if (deliveryResponse.getStatus() < 400) {
      return Response.ok().entity(deliveryResponse.readEntity(DeliveryStatusDTO.class)).build();
    } else {
      String msg = "Failed to make shirts!";
      logger.warn(msg);
      return Response.status(deliveryResponse.getStatus()).entity(msg).build();
    }
  }
}
