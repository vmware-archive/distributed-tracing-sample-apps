package com.wfsample.delivery;

import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.service.DeliveryApi;

import java.util.UUID;

import javax.ws.rs.core.Response;

/**
 * Controller for delivery service which is responsible for dispatching shirts returning tracking
 * number for a given order.
 *
 * @author Hao Song (songhao@vmware.com).
 */
public class DeliveryController implements DeliveryApi {

  @Override
  public Response dispatch(String orderNum, PackedShirtsDTO packedShirts) {
    if (orderNum.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Order Num").build();
    }
    if (packedShirts == null || packedShirts.getShirts() == null ||
        packedShirts.getShirts().size() == 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("no shirts to deliver").build();
    }
    String trackingNum = UUID.randomUUID().toString();
    System.out.println("Tracking number of Order:" + orderNum + " is " + trackingNum);
    return Response.ok(new DeliveryStatusDTO(orderNum, trackingNum,
        "shirts delivery dispatched")).build();
  }

  @Override
  public Response retrieve(String orderNum) {
    if (orderNum.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Order Num").build();
    }
    return Response.ok("Order: " + orderNum + " returned").build();
  }
}
