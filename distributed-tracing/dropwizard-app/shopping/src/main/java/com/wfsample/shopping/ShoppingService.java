package com.wfsample.shopping;

import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;
import com.wavefront.sdk.dropwizard.reporter.WavefrontDropwizardReporter;
import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.Tracing;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.service.StylingApi;

import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
import io.opentracing.Scope;
import io.opentracing.Tracer;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Driver for Shopping service provides consumer facing APIs supporting activities like browsing
 * different styles of beachshirts, and ordering beachshirts.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class ShoppingService extends Application<DropwizardServiceConfig> {
  private static Logger logger = LoggerFactory.getLogger(ShoppingService.class);

  private final Tracer tracer;

  private ShoppingService(Tracer tracer) {
    this.tracer = tracer;
  }

  public static void main(String[] args) throws Exception {
    Tracer tracer = Tracing.init("shopping");
    new ShoppingService(tracer).run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    String stylingUrl = "http://" + configuration.getStylingHost() + ":" + configuration
        .getStylingPort();
    environment.jersey().register(new ShoppingWebResource(
        BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class, this.tracer)));

    ApplicationTags applicationTags = new ApplicationTags.Builder("beachshirts", "shopping").
            cluster("us-west-2").shard("secondary").customTags(new HashMap<String, String>(){{
      put("env", "Staging");
      put("location", "LA");
    }}).build();

    WavefrontSender wfSender = new WavefrontDirectIngestionClient.Builder("https://tracing.wavefront.com",
            "104c7c31-598d-46e2-9972-0fd6c1ec8285").build();

    WavefrontDropwizardReporter wfDropwizardReporter =
            new WavefrontDropwizardReporter.Builder(environment.metrics(), applicationTags).build(wfSender);
    wfDropwizardReporter.start();
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
      try (Scope scope = tracer.buildSpan("getShoppingMenu").startActive(true)) {
        return Response.ok(stylingApi.getAllStyles(httpHeaders)).build();
      }
    }

    @POST
    @Path("/order")
    @Consumes(APPLICATION_JSON)
    public Response orderShirts(OrderDTO orderDTO, @Context HttpHeaders httpHeaders) {
      try (Scope scope = tracer.buildSpan("orderShirts").startActive(true)) {
        if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
          scope.span().setTag(Tags.ERROR.getKey(), true);
          String msg = "Failed to order shirts!";
          logger.warn(msg);
          return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();
        }
        Response deliveryResponse = stylingApi.makeShirts(
            orderDTO.getStyleName(), orderDTO.getQuantity(), httpHeaders);
        if (deliveryResponse.getStatus() < 400) {
          DeliveryStatusDTO deliveryStatus = deliveryResponse.readEntity(DeliveryStatusDTO.class);
          return Response.ok().entity(deliveryStatus).build();
        } else {
          scope.span().setTag(Tags.ERROR.getKey(), true);
          String msg = "Failed to order shirts!";
          logger.warn(msg);
          return Response.status(deliveryResponse.getStatus()).entity(msg).build();
        }
      }
    }
  }
}
