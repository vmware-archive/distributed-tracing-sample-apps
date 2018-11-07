package com.wfsample.shopping;

import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.service.StylingApi;

import java.io.IOException;
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
import io.opentracing.Tracer;

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

  ShoppingController(Tracer tracer) throws IOException {
    String stylingUrl = "http://localhost:50051";
    this.stylingApi = BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class, tracer);
  }

  @GET
  @Path("/menu")
  public Response getShoppingMenu(@Context HttpHeaders httpHeaders) {
    return Response.ok(stylingApi.getAllStyles()).build();
  }

  @POST
  @Path("/order")
  @Consumes(APPLICATION_JSON)
  public Response orderShirts(OrderDTO orderDTO, @Context HttpHeaders httpHeaders) {
    if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Failed to order shirts!").build();
    }
    Response deliveryResponse = stylingApi.makeShirts(
        orderDTO.getStyleName(), orderDTO.getQuantity());
    if (deliveryResponse.getStatus() < 400) {
      DeliveryStatusDTO deliveryStatus = deliveryResponse.readEntity(DeliveryStatusDTO.class);
      return Response.ok().entity(deliveryStatus).build();
    } else {
      return Response.status(deliveryResponse.getStatus()).entity("Failed to order shirts!").build();
    }
  }
}
