package com.wfsample.shopping;

import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.TraceUtils;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.service.StylingApi;

import io.opencensus.common.Scope;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Driver for Shopping service provides consumer facing APIs supporting activities like browsing
 * different styles of beachshirts, and ordering beachshirts.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class ShoppingService extends Application<DropwizardServiceConfig> {
  private static Logger logger = LoggerFactory.getLogger(ShoppingService.class);

  private static final Tracer tracer = Tracing.getTracer();

  private ShoppingService() {
  }


  public static void main(String[] args) throws Exception {
    new ShoppingService().run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    String stylingUrl = "http://" + configuration.getStylingHost() + ":" + configuration
        .getStylingPort();
    // TODO: Initialize WavefrontJaxrsFilter here.

    TraceUtils.initializeTracing(configuration.getShoppingOCAgent());

    //environment.jersey().register(new JaxrsContainerFilter());
    environment.jersey().register(new ShoppingWebResource(
        BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class)));
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
      try (Scope scope = tracer.spanBuilder("getShoppingMenu").startScopedSpan()) {
        return Response.ok(stylingApi.getAllStyles(httpHeaders)).build();
      }
    }

    @POST
    @Path("/order")
    @Consumes(APPLICATION_JSON)
    public Response orderShirts(OrderDTO orderDTO, @Context HttpHeaders httpHeaders) {
      try (Scope scope = tracer.spanBuilder("orderShirts").startScopedSpan()) {
        if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
          tracer.getCurrentSpan().setStatus(Status.INTERNAL);
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
          tracer.getCurrentSpan().setStatus(Status.UNAVAILABLE);
          String msg = "Failed to order shirts!";
          logger.warn(msg);
          return Response.status(deliveryResponse.getStatus()).entity(msg).build();
        }
      }
    }
  }
}
