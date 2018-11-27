package com.wfsample.shopping;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

/**
 * Jersey Configuration class for Delivery Service.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

  @Inject
  public JerseyConfig() {
    register(new ShoppingController());
  }

}