package com.wfsample.common;

import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.Reporter;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;
import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.tag.Tags;
import okhttp3.Request;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Tracing {
  private Tracing() {
  }

  public static io.opentracing.Tracer init(String serviceName) {
    ApplicationTags applicationTags = new ApplicationTags.Builder("beachshirts", serviceName).
            cluster("us-west-2").shard("primary").customTags(new HashMap<String, String>(){{
      put("env", "Staging");
      put("location", "SF");
    }}).build();

    WavefrontSender wfSender = new WavefrontDirectIngestionClient.Builder("https://tracing.wavefront.com",
            "104c7c31-598d-46e2-9972-0fd6c1ec8285").build();

    Reporter wfSpanReporter = new WavefrontSpanReporter.Builder().
            withSource("wavefront-tracing-example").build(wfSender);
    WavefrontTracer.Builder wfTracerBuilder = new WavefrontTracer.
            Builder(wfSpanReporter, applicationTags);
    return wfTracerBuilder.build();
  }

  public static Scope startServerSpan(Tracer tracer, javax.ws.rs.core.HttpHeaders httpHeaders, String operationName) {
    // format the headers for extraction
    MultivaluedMap<String, String> rawHeaders = httpHeaders.getRequestHeaders();
    final HashMap<String, String> headers = new HashMap<>();
    for (String key : rawHeaders.keySet()) {
      headers.put(key, rawHeaders.get(key).get(0));
    }

    Tracer.SpanBuilder spanBuilder;
    try {
      SpanContext parentSpanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers));
      if (parentSpanCtx == null) {
        spanBuilder = tracer.buildSpan(operationName);
      } else {
        spanBuilder = tracer.buildSpan(operationName).asChildOf(parentSpanCtx);
      }
    } catch (IllegalArgumentException e) {
      spanBuilder = tracer.buildSpan(operationName);
    }
    return spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).startActive(true);
  }

  public static TextMap requestBuilderCarrier(final Request.Builder builder) {
    return new TextMap() {
      @Override
      public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("carrier is write-only");
      }

      @Override
      public void put(String key, String value) {
        builder.addHeader(key, value);
      }
    };
  }
}
