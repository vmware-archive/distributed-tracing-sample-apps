package com.wfsample.common;

import com.wavefront.sdk.jaxrs.client.ClientSpanDecorator;
import com.wavefront.sdk.jaxrs.client.ClientTracingFilter;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import java.util.Arrays;
import java.util.List;


/**
 * Utilities for use by the various beachshirts application related services.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public final class BeachShirtsUtils {

  private BeachShirtsUtils() {
  }

  public static <T> T createProxyClient(String url, Class<T> clazz) {
    HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(2000).
            setMaxConnPerRoute(1000).build();
    ApacheHttpClient4Engine apacheHttpClient4Engine = new ApacheHttpClient4Engine(httpClient, true);
    ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
    factory.registerProvider(ResteasyJackson2Provider.class);

    ResteasyClientBuilder resteasyClientBuilder = new ResteasyClientBuilder().
            httpEngine(apacheHttpClient4Engine).providerFactory(factory);

    /**
     * TODO: Make sure context is propagated correctly so that emitted spans belong to the same trace.
     * In order to achieve this, pass in WavefrontTracer to this method and uncomment the 4 lines below
     */
    // List<ClientSpanDecorator> clientSpanDecoratorList = Arrays.asList(
    //     ClientSpanDecorator.STANDARD_TAGS, ClientSpanDecorator.WF_PATH_OPERATION_NAME);
    // ClientTracingFilter filter = new ClientTracingFilter(tracer, clientSpanDecoratorList);
    // resteasyClientBuilder.register(filter);

    ResteasyWebTarget target = resteasyClientBuilder.build().target(url);
    return target.proxy(clazz);
  }
}
