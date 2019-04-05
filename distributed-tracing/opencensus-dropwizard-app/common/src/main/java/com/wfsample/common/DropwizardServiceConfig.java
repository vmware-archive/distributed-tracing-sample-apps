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


  @JsonProperty
  private String ShoppingOCAgent = "localhost:55678";

  @JsonProperty
  private String StylingOCAgent = "localhost:55679";

  @JsonProperty
  private String DeliveryOCAgent = "localhost:55680";


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

  public String getShoppingOCAgent() {
    return ShoppingOCAgent;
  }

  public String getStylingOCAgent() {
    return StylingOCAgent;
  }

  public String getDeliveryOCAgent() {
    return DeliveryOCAgent;
  }
}
