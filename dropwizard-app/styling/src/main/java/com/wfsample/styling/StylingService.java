package com.wfsample.styling;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.WavefrontHistogram;
import com.wavefront.dropwizard.metrics.DropwizardMetricsReporter;
import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.Reporter;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.appagent.jvm.reporter.WavefrontJvmReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;
import com.wavefront.sdk.jersey.WavefrontJerseyFilter;
import com.wavefront.sdk.jersey.reporter.WavefrontJerseyReporter;
import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.service.DeliveryApi;
import com.wfsample.service.StylingApi;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.opentracing.Tracer;

import static java.util.stream.Collectors.toList;

/**
 * Driver for styling service which manages different styles of shirts and takes orders for a shirts
 * of a given style.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class StylingService extends Application<DropwizardServiceConfig> {

  private StylingService() {
  }

  private static String application = "BeachShirts-Frank";
  private static String service = "styling";
  private static String cluster = "frank-laptop";
  private static String shard = "primary";

  private static Map<String, String> customTags = new HashMap<String, String>() {{
    put("location", "Palo Alto");
    put("env", "Local");
  }};

  private static WavefrontDirectIngestionClient.Builder builder;
  private static ApplicationTags applicationTags;
  private static WavefrontJerseyReporter wfJerseyReporter;
  private static Tracer tracer;
  private static WavefrontHistogram wavefrontHistogram;


    private static MetricRegistry metricRegistry;

  public static void main(String[] args) throws Exception {
    new StylingService().run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    String deliveryUrl = "http://" + configuration.getDeliveryHost() + ":" + configuration
        .getDeliveryPort();


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
    tracer = new WavefrontTracer.Builder(wfSpanReporter, applicationTags).build();
    WavefrontJerseyFilter.Builder wfJerseyFilterBuilder =
            new WavefrontJerseyFilter.Builder(wfJerseyReporter, applicationTags);

    // Set the tracer to optionally send tracing data
    wfJerseyFilterBuilder.withTracer(tracer);

    // Create the WavefrontJerseyFilter
    WavefrontJerseyFilter wfJerseyFilter = wfJerseyFilterBuilder.build();


    WavefrontJvmReporter.Builder jvmBuilder = new WavefrontJvmReporter.Builder(applicationTags);

    // Optinal: Set the source for your metrics and histograms
    // Defaults to hostname if omitted
    //jvmBuilder.withSource("mySource");

    // Optional: change the reporting frequency to 30 seconds, defaults to 1 min
    jvmBuilder.reportingIntervalSeconds(30);

    // Create a WavefrontJvmReporter using ApplicationTags metadata and WavefronSender
    WavefrontJvmReporter wfJvmReporter = new WavefrontJvmReporter.
            Builder(applicationTags).
            build(wavefrontSender);

    wfJvmReporter.start();





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

      DropwizardMetricsReporter dropwizardMetricsReporter = dwBuilder.reportMinuteDistribution().build(wavefrontSender);
      dropwizardMetricsReporter.start(10, TimeUnit.SECONDS);

      environment.jersey().register(new StylingWebResource(
              BeachShirtsUtils.createProxyClient(deliveryUrl, DeliveryApi.class, tracer)));

      // Register the filter with Dropwizard Jersey Environment
      environment.jersey().register(wfJerseyFilter);
      wfJerseyReporter.start();

      wavefrontHistogram = WavefrontHistogram.get(metricRegistry, "shirt-quantity");
  }

  public class StylingWebResource implements StylingApi {
    // sample set of static styles.
    private List<ShirtStyleDTO> shirtStyleDTOS = new ArrayList<>();
    private final DeliveryApi deliveryApi;

    StylingWebResource(DeliveryApi deliveryApi) {
      this.deliveryApi = deliveryApi;
      ShirtStyleDTO dto = new ShirtStyleDTO();
      dto.setName("style1");
      dto.setImageUrl("style1Image");
      ShirtStyleDTO dto2 = new ShirtStyleDTO();
      dto2.setName("style2");
      dto2.setImageUrl("style2Image");
      shirtStyleDTOS.add(dto);
      shirtStyleDTOS.add(dto2);
    }

    public List<ShirtStyleDTO> getAllStyles() {
      return shirtStyleDTOS;
    }

    public Response makeShirts(String id, int quantity) {
      /*
       * TODO: Try to report the value of quantity using WavefrontHistogram.
       * Important: Make sure you are sending to Minute bin instead of Hour or Day bin!
       *
       * Viewing the quantity requested by various clients as a minute distribution and then
       * applying statistical functions (median, mean, min, max, p95, p99 etc.) on that data is
       * really useful to understand the user trend.
       */

      wavefrontHistogram.update(quantity);

      if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Failed to make shirts!").build();
      }
      String orderNum = UUID.randomUUID().toString();
      List<ShirtDTO> packedShirts = new ArrayList<>();
      for (int i = 0; i < quantity; i++) {
        packedShirts.add(new ShirtDTO(new ShirtStyleDTO(id, id + "Image")));
      }
      PackedShirtsDTO packedShirtsDTO = new PackedShirtsDTO(packedShirts.stream().
          map(shirt -> new ShirtDTO(
              new ShirtStyleDTO(shirt.getStyle().getName(), shirt.getStyle().getImageUrl()))).
          collect(toList()));
      Response deliveryResponse = deliveryApi.dispatch(orderNum, packedShirtsDTO);
      if (deliveryResponse.getStatus() < 400) {
        return Response.ok().entity(deliveryResponse.readEntity(DeliveryStatusDTO.class)).build();
      } else {
        return Response.status(deliveryResponse.getStatus()).entity("Failed to make shirts").build();
      }
    }
  }
}