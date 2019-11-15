package com.wfsample.shopping;

import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.Tracing;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.OrderDTO;
import com.wfsample.service.StylingApi;

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
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;

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
      Span span = tracer.buildSpan("getShoppingMenu").start();
      try (Scope scope = tracer.scopeManager().activate(span)) {
        return Response.ok(stylingApi.getAllStyles(httpHeaders)).build();
      } catch(Exception ex) {
        Tags.ERROR.set(span, true);
        span.log(new HashMap<String, Object>() {{
          put(Fields.EVENT, "error");
          put(Fields.ERROR_OBJECT, ex);
          put(Fields.MESSAGE, ex.getMessage());
        }});
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
      } finally {
        span.finish();
      }
    }

    @POST
    @Path("/order")
    @Consumes(APPLICATION_JSON)
    public Response orderShirts(OrderDTO orderDTO, @Context HttpHeaders httpHeaders) {
      Span span = tracer.buildSpan("orderShirts").start();
      try (Scope scope = tracer.scopeManager().activate(span)) {
        if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
          span.setTag(Tags.ERROR.getKey(), true);
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
          span.setTag(Tags.ERROR.getKey(), true);
          String msg = "Failed to order shirts!";
          logger.warn(msg);
          return Response.status(deliveryResponse.getStatus()).entity(msg).build();
        }
      } catch(Exception ex) {
        Tags.ERROR.set(span, true);
        span.log(new HashMap<String, Object>() {{
          put(Fields.EVENT, "error");
          put(Fields.ERROR_OBJECT, ex);
          put(Fields.MESSAGE, ex.getMessage());
        }});
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
      } finally {
        span.finish();
      }
    }
  }
}
