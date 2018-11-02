package com.wfsample.shopping;

import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.service.StylingApi;

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

  ShoppingController() {
    String stylingUrl = "http://localhost:50051";
    this.stylingApi = BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class);
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
    DeliveryStatusDTO deliveryStatus = stylingApi.makeShirts(
        orderDTO.getStyleName(), orderDTO.getQuantity());
    return Response.ok().entity(deliveryStatus).build();
  }
}
