package com.wfsample.common;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFilter;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Utilities for use by the various beachshirts application related services.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public final class BeachShirtsUtils {

  private BeachShirtsUtils() {
  }

  public static <T> T createProxyClient(String url, Class<T> clazz, Tracer tracer) {
    HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(2000).
        setMaxConnPerRoute(1000).build();
    ApacheHttpClient4Engine apacheHttpClient4Engine = new ApacheHttpClient4Engine(httpClient, true);
    ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
    factory.registerProvider(ResteasyJackson2Provider.class);

    ResteasyClientBuilder resteasyClientBuilder = new ResteasyClientBuilder().
        httpEngine(apacheHttpClient4Engine).providerFactory(factory);

    /**
     * TODO: Make sure context is propagated correctly so that emitted spans belong to the same trace.
     * In order to achieve this, pass in WavefrontTracer to this method and uncomment the 2 lines below
     */
    ClientTracingFilter filter = new ClientTracingFilter(tracer, new ArrayList<>());
    resteasyClientBuilder.register(filter);

    ResteasyWebTarget target = resteasyClientBuilder.build().target(url);
    return target.proxy(clazz);
  }
}
