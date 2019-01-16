package com.wfsample.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

/**
 * Dropwizard based service configurtion.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class DropwizardServiceConfig extends Configuration {

  /**
   * Port on which the styling service is running.
   */
  @JsonProperty
  private int stylingPort = 50051;

  /**
   * Port on which delivery service is running.
   */
  @JsonProperty
  private int deliveryPort = 50052;

  /**
   * Host on which the styling service is running.
   */
  @JsonProperty
  private String stylingHost = "stylingService";

  /**
   * Host on which the delivery service is running.
   */
  @JsonProperty
  private String deliveryHost = "deliveryService";

  public int getStylingPort() {
    return stylingPort;
  }

  public int getDeliveryPort() {
    return deliveryPort;
  }

  public String getStylingHost() {
    return stylingHost;
  }

  public String getDeliveryHost() {
    return deliveryHost;
  }
}
