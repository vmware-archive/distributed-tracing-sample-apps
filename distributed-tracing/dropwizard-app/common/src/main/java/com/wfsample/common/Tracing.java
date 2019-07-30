package com.wfsample.common;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.Configuration.ReporterConfiguration;
import com.uber.jaeger.Configuration.SamplerConfiguration;
import com.uber.jaeger.samplers.ConstSampler;
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

  public static com.uber.jaeger.Tracer init(String service) {
    SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv()
        .withType(ConstSampler.TYPE)
        .withParam(1);

    ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv();

    Configuration config = new Configuration(service)
        .withSampler(samplerConfig)
        .withReporter(reporterConfig);

    return (com.uber.jaeger.Tracer) config.getTracer();
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
    return spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).
        withTag("env", "staging").withTag("location", "palo-alto").withTag("tenant", "wavefront").
        startActive(true);
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
