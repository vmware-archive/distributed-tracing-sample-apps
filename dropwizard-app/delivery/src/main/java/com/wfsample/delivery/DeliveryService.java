package com.wfsample.delivery;

import com.codahale.metrics.Counter;
import com.codahale.metrics.DeltaCounter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.wavefront.dropwizard.metrics.DropwizardMetricsReporter;
import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.Reporter;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.appagent.jvm.reporter.WavefrontJvmReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;
import com.wavefront.sdk.jersey.WavefrontJerseyFilter;
import com.wavefront.sdk.jersey.reporter.WavefrontJerseyReporter;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.service.DeliveryApi;
import com.wavefront.sdk.common.application.ApplicationTags;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import io.opentracing.Tracer;

/**
 * Driver for styling service which manages different styles of shirts and takes orders for a shirts
 * of a given style.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class DeliveryService extends Application<DropwizardServiceConfig> {
  /*
   * TODO: Add a gauge to monitor the size of dispatch queue.
   * DONE
   * Also, consider adding relevant ApplicationTags for this metric.
   */

  private static String application = "BeachShirts-Frank";
  private static String service = "delivery";
  private static String cluster = "frank-laptop";
  private static String shard = "primary";

  private static Map<String, String> customTags = new HashMap<String, String>() {{
    put("location", "Palo Alto");
    put("env", "Local");
  }};

  private static ApplicationTags applicationTags;
  private static WavefrontJerseyReporter wfJerseyReporter;

  // Create a builder with the URL of the form "https://DOMAIN.wavefront.com"
// and a Wavefront API token with direct ingestion permission
  private static WavefrontDirectIngestionClient.Builder builder;
  private static Queue<PackedShirtsDTO> dispatchQueue;

  private static MetricRegistry metricRegistry;

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


      applicationTags = new ApplicationTags.Builder(application, service).
              cluster(cluster).       // optional
              shard(shard).           // optional
              customTags(customTags). // optional
              build();

      String wavefrontURL = "https://tracing.wavefront.com/";
      String token = "f0387e37-533d-4e17-94d7-f8e1ca320767";

      builder = new WavefrontDirectIngestionClient.Builder(wavefrontURL, token);

      // Optional configuration properties.
      // Only override the defaults to set higher values.

      // This is the size of internal buffer beyond which data is dropped
      // Optional: Set this to override the default max queue size of 50,000
      builder.maxQueueSize(100_000);

      // This is the max batch of data sent per flush interval
      // Optional: Set this to override the default batch size of 10,000
      builder.batchSize(20_000);

      // Together with batch size controls the max theoretical throughput of the sender
      // Optional: Set this to override the default flush interval value of 1 second
      builder.flushIntervalSeconds(2);

      // Finally create a WavefrontDirectIngestionClient
      WavefrontSender wavefrontSender = builder.build();

      // Create WavefrontJerseyReporter.Builder using applicationTags.
      WavefrontJerseyReporter.Builder builder = new WavefrontJerseyReporter.Builder(applicationTags);

      // Optionally set a nondefault source name for your metrics and histograms. Omit this statement to use the host name.
      //builder.withSource("mySource");

      // Optionally change the reporting interval to 30 seconds. Default is 1 minute
      builder.reportingIntervalSeconds(30);

      // Create a WavefrontJerseyReporter with a WavefronSender
      wfJerseyReporter = builder.build(wavefrontSender);

      Reporter wfSpanReporter = null;
      try {
          wfSpanReporter = new WavefrontSpanReporter.Builder().build(wavefrontSender);
      } catch (IOException e) {
          e.printStackTrace();
      }
      Tracer tracer = new WavefrontTracer.Builder(wfSpanReporter, applicationTags).build();
      WavefrontJerseyFilter.Builder wfJerseyFilterBuilder =
              new WavefrontJerseyFilter.Builder(wfJerseyReporter, applicationTags);

      // Set the tracer to optionally send tracing data
      wfJerseyFilterBuilder.withTracer(tracer);

      // Create the WavefrontJerseyFilter
      WavefrontJerseyFilter wfJerseyFilter = wfJerseyFilterBuilder.build();

      WavefrontJvmReporter.Builder jvmBuilder = new WavefrontJvmReporter.Builder(applicationTags);

     // Optinal: Set the source for your metrics and histograms
     // Defaults to hostname if omitted
      // jvmBuilder.withSource("mySource");

      // Optional: change the reporting frequency to 30 seconds, defaults to 1 min
      jvmBuilder.reportingIntervalSeconds(30);


// Create a WavefrontJvmReporter using ApplicationTags metadata and WavefronSender
      WavefrontJvmReporter wfJvmReporter = new WavefrontJvmReporter.
              Builder(applicationTags).
              build(wavefrontSender);

      wfJvmReporter.start();


      // Register the filter with Dropwizard Jersey Environment
      environment.jersey().register(wfJerseyFilter);
      wfJerseyReporter.start();


      // Create a registry
      metricRegistry = new MetricRegistry();

      // Create a builder instance for the registry
      DropwizardMetricsReporter.Builder dwBuilder = DropwizardMetricsReporter.forRegistry(metricRegistry);

      // Optional: Set a nondefault source for your metrics and histograms.
      // Defaults to hostname if omitted
      //dwBuilder.withSource("mySource");

      // Add individual reporter-level point tags for your metrics and histograms
      // The point tags are sent with every metric and histogram reported to Wavefront.
      dwBuilder.withReporterPointTag("env", "local");  // Example - replace values!
      dwBuilder.withReporterPointTag("location", "Palo Alto");  // Example - replace values!

      // Optional: Add application tags, which are propagated as point tags with the reported metric.
      // See https://github.com/wavefrontHQ/wavefront-sdk-java/blob/master/docs/apptags.md for details.
      dwBuilder.withApplicationTags(applicationTags);   // Example - replace values!

      DropwizardMetricsReporter dropwizardMetricsReporter = dwBuilder.build(wavefrontSender);
      dropwizardMetricsReporter.start(10, TimeUnit.SECONDS);


      Gauge gauge = metricRegistry.register("dispatch-queue-size", () -> dispatchQueue.size());
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
      DeltaCounter shirtsDelivered = DeltaCounter.get(metricRegistry, "shirts-delivered");
      for (int i = 0; i < packedShirtsDTO.getShirts().size(); i++) {
        /*
         * TODO: Try to Increment a delta counter when shirts are delivered.
         * DONE
         * Also, consider adding relevant ApplicationTags for this metric.
         */
        shirtsDelivered.inc();
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
          Counter counter = metricRegistry.counter("failed-to-dispatch");
          counter.inc();
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Failed to dispatch " +
            "shirts!").build();
      }
      if (orderNum.isEmpty()) {
          Counter counter = metricRegistry.counter("order-empty-error");
          counter.inc();
        /*
         * TODO: Try to emitting an error counter to Wavefront.
         * Also, consider adding relevant ApplicationTags for this metric.
         */
        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Order Num").build();
      }
      if (packedShirts == null || packedShirts.getShirts() == null ||
          packedShirts.getShirts().size() == 0) {
        /*
         * TODO: Try to emitting an error counter to Wavefront.
         * Also, consider adding relevant ApplicationTags for this metric.
         */
          Counter counter = metricRegistry.counter("packed-shirts-error");
          counter.inc();
        return Response.status(Response.Status.BAD_REQUEST).entity("no shirts to deliver").build();
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
         * TODO: Try to emitting an error counter to Wavefront.
         * Also, consider adding relevant ApplicationTags for this metric.
         */
          Counter counter = metricRegistry.counter("order-empty-error");
          counter.inc();
        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Order Num").build();
      }
      return Response.ok("Order: " + orderNum + " returned").build();
    }
  }
}