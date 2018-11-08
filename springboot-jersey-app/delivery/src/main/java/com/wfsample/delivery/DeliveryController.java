package com.wfsample.delivery;

import com.codahale.metrics.Counter;
import com.codahale.metrics.DeltaCounter;
import com.codahale.metrics.Gauge;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.service.DeliveryApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.core.Response;

import static com.wfsample.common.BeachShirtsUtils.METRIC_REGISTRY;

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
  private static final Gauge<Integer> queuesize = METRIC_REGISTRY.register("delivery.queue_size", () -> dispatchQueue.size());
  private final Counter invalidDispatchOrders = METRIC_REGISTRY.counter("delivery.dispatch.invalid_orders");
  private final Counter invalidRetrieveOrders = METRIC_REGISTRY.counter("delivery.retrieve.invalid_orders");
  private final Counter noShirts = METRIC_REGISTRY.counter("delivery.dispatch.no_shirts");
  private final DeltaCounter delivered = DeltaCounter.get(METRIC_REGISTRY, "delivery.shirts_delivered");
  private static Logger logger = LoggerFactory.getLogger(DeliveryService.class);

  public DeliveryController() {
    dispatchQueue = new ConcurrentLinkedDeque<>();
  }

  @Override
  public Response dispatch(String orderNum, PackedShirtsDTO packedShirts) {
    if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
      String msg = "Failed to dispatch shirts!";
      logger.warn(msg);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();
    }
    if (orderNum.isEmpty()) {
      /*
       * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
       */
      invalidDispatchOrders.inc();
      String msg = "Invalid Order Num";
      logger.warn(msg);
      return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
    }
    if (packedShirts == null || packedShirts.getShirts() == null ||
        packedShirts.getShirts().size() == 0) {
      /*
       * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
       */
      noShirts.inc();
      String msg = "No shirts to deliver";
      logger.warn(msg);
      return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
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
      delivered.inc();
    }
    System.out.println(packedShirtsDTO.getShirts().size() + " shirts delivered!");
  }

  @Override
  public Response retrieve(String orderNum) {
    if (orderNum.isEmpty()) {
      /*
       * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
       */
      invalidRetrieveOrders.inc();
      String msg = "Invalid Order Num";
      logger.warn(msg);
      return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
    }
    return Response.ok("Order: " + orderNum + " returned").build();
  }
}
