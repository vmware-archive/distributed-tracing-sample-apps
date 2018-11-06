package com.wfsample.common;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

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
    ResteasyClient resteasyClient = new ResteasyClientBuilder().
        httpEngine(apacheHttpClient4Engine).providerFactory(factory).build();
    ResteasyWebTarget target = resteasyClient.target(url);
    return target.proxy(clazz);
  }

}
