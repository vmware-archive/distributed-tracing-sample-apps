package com.wfsample.service;

import com.wfsample.common.dto.PackedShirtsDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API interface for delivery service.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@Path("/delivery")
@Produces(MediaType.APPLICATION_JSON)
public interface DeliveryApi {

  @POST
  @Path("dispatch/{orderNum}")
  @Consumes(MediaType.APPLICATION_JSON)
  Response dispatch(@PathParam("orderNum") String orderNum, PackedShirtsDTO shirts);

  @POST
  @Path("return/{orderNum}")
  Response retrieve(@PathParam("orderNum") String orderNum);
}