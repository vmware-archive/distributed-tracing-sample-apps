package com.wfsample.shopping;

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
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.service.StylingApi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.opentracing.Tracer;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Driver for Shopping service provides consumer facing APIs supporting activities like browsing
 * different styles of beachshirts, and ordering beachshirts.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class ShoppingService extends Application<DropwizardServiceConfig> {

  private ShoppingService() {
  }

  private static String application = "BeachShirts-Frank";
  private static String service = "shopping";
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

  public static void main(String[] args) throws Exception {
    new ShoppingService().run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    String stylingUrl = "http://" + configuration.getStylingHost() + ":" + configuration
        .getStylingPort();



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





    environment.jersey().register(new ShoppingWebResource(
            BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class, tracer)));

    // Register the filter with Dropwizard Jersey Environment
    environment.jersey().register(wfJerseyFilter);
    wfJerseyReporter.start();
  }

  @Path("/shop")
  @Produces(MediaType.APPLICATION_JSON)
  public class ShoppingWebResource {
    private final StylingApi stylingApi;

    ShoppingWebResource(StylingApi stylingApi) {
      this.stylingApi = stylingApi;
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
}
