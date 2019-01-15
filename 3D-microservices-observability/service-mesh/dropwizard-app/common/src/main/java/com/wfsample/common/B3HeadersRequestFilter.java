package com.wfsample.common;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

/**
 * A request filter to propagate the B3 http headers required to enable proper trace hierarcy in
 * Istio.
 *
 * @author Anil Kodali (akodali@vmware.com)
 */
public class B3HeadersRequestFilter implements ClientRequestFilter{

  private HttpHeaders httpHeaders;

  public B3HeadersRequestFilter() {
  }

  @Override
  public void filter(ClientRequestContext clientRequest) throws IOException {
    for (Map.Entry<String, List<String>> httpheader : httpHeaders.getRequestHeaders().entrySet()) {
      // Set B3-headers required for tracing in Istio as per
      // https://istio.io/docs/tasks/telemetry/distributed-tracing/#understanding-what-happened
      switch (httpheader.getKey()) {
        case "x-request-id":
          clientRequest.getHeaders().add(httpheader.getKey(), httpheader.getValue().get(0));
          continue;
        case "x-b3-traceid":
          clientRequest.getHeaders().add(httpheader.getKey(), httpheader.getValue().get(0));
          continue;
        case "x-b3-spanid":
          clientRequest.getHeaders().add(httpheader.getKey(), httpheader.getValue().get(0));
          continue;
        case "x-b3-parentspanid":
          clientRequest.getHeaders().add(httpheader.getKey(), httpheader.getValue().get(0));
          continue;
        case "x-b3-sampled":
          clientRequest.getHeaders().add(httpheader.getKey(), httpheader.getValue().get(0));
          continue;
        case "x-b3-flags":
          clientRequest.getHeaders().add(httpheader.getKey(), httpheader.getValue().get(0));
          continue;
        case "x-ot-span-context":
          clientRequest.getHeaders().add(httpheader.getKey(), httpheader.getValue().get(0));
          continue;
        default:
      }
    }
  }

  public void setB3Headers(HttpHeaders httpHeaders) {
    this.httpHeaders = httpHeaders;
  }

}
