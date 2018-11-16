package com.wfsample.delivery;

import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.service.DeliveryApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import io.dropwizard.Application;
import io.dropwizard.lifecycle.setup.ScheduledExecutorServiceBuilder;
import io.dropwizard.setup.Environment;

/**
 * Driver for styling service which manages different styles of shirts and takes orders for a shirts
 * of a given style.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class DeliveryService extends Application<DropwizardServiceConfig> {
  /*
   * TODO: Add a gauge to monitor the size of dispatch queue.
   * Also, consider adding relevant ApplicationTags for this metric.
   */
  private static Queue<PackedShirtsDTO> dispatchQueue;
  private static Logger logger = LoggerFactory.getLogger(DeliveryService.class);

  private DeliveryService() {
  }

  public static void main(String[] args) throws Exception {
    new DeliveryService().run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    dispatchQueue = new ConcurrentLinkedDeque<>();
    environment.jersey().register(new DeliveryWebResource());
    ScheduledExecutorServiceBuilder sesBuilder =
        environment.lifecycle().scheduledExecutorService("Clear Queue");
    ScheduledExecutorService ses = sesBuilder.build();
    Runnable clearQueueTask = new ClearQueueTask();
    ses.scheduleWithFixedDelay(clearQueueTask, 30, 30, TimeUnit.SECONDS);
  }

  private static final class ClearQueueTask implements Runnable {
    @Override
    public void run() {
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
  }

  public class DeliveryWebResource implements DeliveryApi {

    DeliveryWebResource() {
    }

    @Override
    public Response dispatch(String orderNum, PackedShirtsDTO packedShirts) {
      if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
        String msg = "Failed to dispatch shirts!";
        logger.warn(msg);
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();
      }
      if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
        orderNum = "";
      }
      if (orderNum.isEmpty()) {
        /*
         * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
         */
        String msg = "Invalid Order Num";
        logger.warn(msg);
        return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
      }
      if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
        packedShirts = null;
      }
      if (packedShirts == null || packedShirts.getShirts() == null ||
          packedShirts.getShirts().size() == 0) {
        /*
         * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
         */
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

    @Override
    public Response retrieve(String orderNum) {
      if (orderNum.isEmpty()) {
        /*
         * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
         */
        String msg = "Invalid Order Num";
        logger.warn(msg);
        return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
      }
      return Response.ok("Order: " + orderNum + " returned").build();
    }
  }
}