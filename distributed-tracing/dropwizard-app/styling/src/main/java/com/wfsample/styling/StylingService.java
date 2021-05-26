package com.wfsample.styling;

import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.common.dto.DeliveryStatusDTO;
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

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import static java.util.stream.Collectors.toList;

/**
 * Driver for styling service which manages different styles of shirts and takes orders for a shirts
 * of a given style.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class StylingService extends Application<DropwizardServiceConfig> {
  private static final Logger logger = LoggerFactory.getLogger(StylingService.class);

  public static void main(String[] args) throws Exception {
    new StylingService().run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    String deliveryUrl = "http://" + configuration.getDeliveryHost() + ":" + configuration
        .getDeliveryPort();
    environment.jersey().register(new StylingWebResource(
        BeachShirtsUtils.createProxyClient(deliveryUrl, DeliveryApi.class)));
  }

  public class StylingWebResource implements StylingApi {
    // sample set of static styles.
    private final List<ShirtStyleDTO> shirtStyleDTOS = new ArrayList<>();
    private final DeliveryApi deliveryApi;

    StylingWebResource(DeliveryApi deliveryApi) {
      this.deliveryApi = deliveryApi;
      ShirtStyleDTO dto = new ShirtStyleDTO();
      dto.setName("style1");
      dto.setImageUrl("style1Image");
      ShirtStyleDTO dto2 = new ShirtStyleDTO();
      dto2.setName("style2");
      dto2.setImageUrl("style2Image");
      shirtStyleDTOS.add(dto);
      shirtStyleDTOS.add(dto2);
    }

    @Override
    public List<ShirtStyleDTO> getAllStyles(HttpHeaders httpHeaders) {
        return shirtStyleDTOS;
    }

    @Override
    public Response makeShirts(String id, int quantity, HttpHeaders httpHeaders) {
        if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
          String msg = "Failed to make shirts!";
          logger.warn(msg);
          return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();
        }
        String orderNum = UUID.randomUUID().toString();
        List<ShirtDTO> packedShirts = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
          packedShirts.add(new ShirtDTO(new ShirtStyleDTO(id, id + "Image")));
        }
        PackedShirtsDTO packedShirtsDTO = new PackedShirtsDTO(packedShirts.stream().
            map(shirt -> new ShirtDTO(
                new ShirtStyleDTO(shirt.getStyle().getName(), shirt.getStyle().getImageUrl()))).
            collect(toList()));
        Response deliveryResponse = deliveryApi.dispatch(orderNum, packedShirtsDTO, httpHeaders);
        if (deliveryResponse.getStatus() < 400) {
          return Response.ok().entity(deliveryResponse.readEntity(DeliveryStatusDTO.class)).build();
        } else {
          String msg = "Failed to make shirts!";
          logger.warn(msg);
          return Response.status(deliveryResponse.getStatus()).entity(msg).build();
        }
    }
  }
}