package com.wfsample.delivery;

import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;
import com.wavefront.sdk.dropwizard.reporter.WavefrontDropwizardReporter;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.Tracing;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.service.DeliveryApi;

import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.dropwizard.Application;
import io.dropwizard.lifecycle.setup.ScheduledExecutorServiceBuilder;
import io.dropwizard.setup.Environment;
import io.opentracing.Scope;
import io.opentracing.Tracer;

/**
 * Driver for styling service which manages different styles of shirts and takes orders for a shirts
 * of a given style.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class DeliveryService extends Application<DropwizardServiceConfig> {
  private static Queue<PackedShirtsDTO> dispatchQueue;
  private static Logger logger = LoggerFactory.getLogger(DeliveryService.class);

  private final Tracer tracer;

  private DeliveryService(Tracer tracer) {
    this.tracer = tracer;
  }

  public static void main(String[] args) throws Exception {
    Tracer tracer = Tracing.init("delivery");
    new DeliveryService(tracer).run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    dispatchQueue = new ConcurrentLinkedDeque<>();
    environment.jersey().register(new DeliveryWebResource());

    ApplicationTags applicationTags = new ApplicationTags.Builder("beachshirts", "delivery").
            cluster("us-west-2").shard("primary").customTags(new HashMap<String, String>(){{
              put("env", "Staging");
              put("location", "SF");
            }}).build();

    WavefrontSender wfSender = new WavefrontDirectIngestionClient.Builder("https://tracing.wavefront.com",
            "104c7c31-598d-46e2-9972-0fd6c1ec8285").build();

    WavefrontDropwizardReporter wfDropwizardReporter =
            new WavefrontDropwizardReporter.Builder(environment.metrics(), applicationTags).build(wfSender);
    wfDropwizardReporter.start();

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
      System.out.println(packedShirtsDTO.getShirts().size() + " shirts delivered!");
    }
  }

  public class DeliveryWebResource implements DeliveryApi {

    DeliveryWebResource() {
    }

    @Override
    public Response dispatch(String orderNum, PackedShirtsDTO packedShirts, HttpHeaders httpHeaders) {
      try (Scope scope = Tracing.startServerSpan(tracer, httpHeaders, "dispatch")) {
        if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
          String msg = "Failed to dispatch shirts!";
          logger.warn(msg);
          return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();
        }
        if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
          orderNum = "";
        }
        if (orderNum.isEmpty()) {
          String msg = "Invalid Order Num";
          logger.warn(msg);
          scope.span().setTag(Tags.ERROR.getKey(), true);
          return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
          packedShirts = null;
        }
        if (packedShirts == null || packedShirts.getShirts() == null ||
            packedShirts.getShirts().size() == 0) {
          String msg = "No shirts to deliver";
          logger.warn(msg);
          scope.span().setTag(Tags.ERROR.getKey(), true);
          return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        dispatchQueue.add(packedShirts);
        String trackingNum = UUID.randomUUID().toString();
        System.out.println("Tracking number of Order:" + orderNum + " is " + trackingNum);
        return Response.ok(new DeliveryStatusDTO(orderNum, trackingNum,
            "shirts delivery dispatched")).build();
      }
    }

    @Override
    public Response retrieve(String orderNum, HttpHeaders httpHeaders) {
      try (Scope scope = Tracing.startServerSpan(tracer, httpHeaders, "retrieve")) {
        if (orderNum.isEmpty()) {
          String msg = "Invalid Order Num";
          logger.warn(msg);
          scope.span().setTag(Tags.ERROR.getKey(), true);
          return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        return Response.ok("Order: " + orderNum + " returned").build();
      }
    }
  }
}