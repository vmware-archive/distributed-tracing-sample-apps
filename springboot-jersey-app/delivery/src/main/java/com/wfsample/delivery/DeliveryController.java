package com.wfsample.delivery;

import com.codahale.metrics.Gauge;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.service.DeliveryApi;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.Response;

/**
 * Controller for delivery service which is responsible for dispatching shirts returning tracking
 * number for a given order.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@Component
public class DeliveryController implements DeliveryApi {
  /*
   * TODO: Add a gauge to monitor the size of dispatch queue.
   * Also, consider adding relevant ApplicationTags for this metric.
   */
  private static Queue<PackedShirtsDTO> dispatchQueue;

  public DeliveryController() {
    dispatchQueue = new ConcurrentLinkedDeque<>();
  }

  @Override
  public Response dispatch(String orderNum, PackedShirtsDTO packedShirts) {
    if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Failed to dispatch " +
          "shirts!").build();
    }
    if (orderNum.isEmpty()) {
      /*
       * TODO: Try to emit an error counter to Wavefront.
       * Also, consider adding relevant ApplicationTags for this metric.
       */
      return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Order Num").build();
    }
    if (packedShirts == null || packedShirts.getShirts() == null ||
        packedShirts.getShirts().size() == 0) {
      /*
       * TODO: Try to emit an error counter to Wavefront.
       * Also, consider adding relevant ApplicationTags for this metric.
       */
      return Response.status(Response.Status.BAD_REQUEST).entity("no shirts to deliver").build();
    }
    dispatchQueue.add(packedShirts);
    String trackingNum = UUID.randomUUID().toString();
    System.out.println("Tracking number of Order:" + orderNum + " is " + trackingNum);
    return Response.ok(new DeliveryStatusDTO(orderNum, trackingNum,
        "shirts delivery dispatched")).build();
  }

  @Scheduled(fixedRate = 30000)
  private void processQueue() {
    System.out.println("Processing " + dispatchQueue.size() + " in the Dispatch Queue!");
    while (!dispatchQueue.isEmpty()) {
      deliverPackedShirts(dispatchQueue.poll());
    }
  }

  private void deliverPackedShirts(PackedShirtsDTO packedShirtsDTO) {
    for (int i = 0; i < packedShirtsDTO.getShirts().size(); i++) {
      /*
      * TODO: Try to Increment a delta counter when shirts are delivered.
      * Also, consider adding relevant ApplicationTags for this metric.
      */
    }
    System.out.println(packedShirtsDTO.getShirts().size() + " shirts delivered!");
  }

  @Override
  public Response retrieve(String orderNum) {
    if (orderNum.isEmpty()) {
      /*
       * TODO: Try to emit an error counter to Wavefront.
       * Also, consider adding relevant ApplicationTags for this metric.
       */
      return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Order Num").build();
    }
    return Response.ok("Order: " + orderNum + " returned").build();
  }
}
