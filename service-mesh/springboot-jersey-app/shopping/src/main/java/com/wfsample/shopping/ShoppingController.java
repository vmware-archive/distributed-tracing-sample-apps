package com.wfsample.shopping;

import com.wavefront.sdk.jaxrs.client.WavefrontJaxrsClientFilter;
import com.wfsample.common.B3HeadersRequestFilter;
import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.service.StylingApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Driver for Shopping service provides consumer facing APIs supporting activities like browsing
 * different styles of beachshirts, and ordering beachshirts.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
@Path("/shop")
@Produces(MediaType.APPLICATION_JSON)
public class ShoppingController {
  private final StylingApi stylingApi;
  private B3HeadersRequestFilter b3Filter;
  private static Logger logger = LoggerFactory.getLogger(ShoppingService.class);

  ShoppingController() {
    // Use the service alias for styling service in Kubernetes.
    String stylingUrl = "http://stylingservice:50051";
    // Uncomment the below line to use localhost instead.
    // String stylingUrl = "http://localhost:50051";

    // wavefrontJaxrsFilter = wfJaxrsClientFilter;
    // Filter to propagate B3 headers.
    b3Filter = new B3HeadersRequestFilter();
    this.stylingApi = BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class, b3Filter);
  }

  @GET
  @Path("/menu")
  public Response getShoppingMenu(@Context HttpHeaders httpHeaders) {
    // Propagate B3 headers.
    b3Filter.setB3Headers(httpHeaders);
    return Response.ok(stylingApi.getAllStyles(httpHeaders)).build();
  }

  @POST
  @Path("/order")
  @Consumes(APPLICATION_JSON)
  public Response orderShirts(OrderDTO orderDTO, @Context HttpHeaders httpHeaders) {
    if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
      String msg = "Failed to order shirts!";
      logger.warn(msg);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();    }
    // Propagate B3 headers.
    b3Filter.setB3Headers(httpHeaders);
    Response deliveryResponse = stylingApi.makeShirts(
        orderDTO.getStyleName(), orderDTO.getQuantity(), httpHeaders);
    if (deliveryResponse.getStatus() < 400) {
      DeliveryStatusDTO deliveryStatus = deliveryResponse.readEntity(DeliveryStatusDTO.class);
      return Response.ok().entity(deliveryStatus).build();
    } else {
      String msg = "Failed to order shirts!";
      logger.warn(msg);
      return Response.status(deliveryResponse.getStatus()).entity(msg).build();    }
  }
}
