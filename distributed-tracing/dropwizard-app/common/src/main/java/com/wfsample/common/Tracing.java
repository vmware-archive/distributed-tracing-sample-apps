package com.wfsample.common;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Random;

public final class Tracing {
  private final static Random RAND = new Random(System.currentTimeMillis());
  private final static String[] ENV_TAGS = new String[] {"staging", "production", "development"};
  private final static String[] LOCATION_TAGS = new String[] {"palo-alto", "san-francisco",
      "new-york"};
  private final static String[] TENANT_TAGS = new String[] {"wavefront", "vmware"};

  private Tracing() {
  }

  public static io.opentracing.Tracer init(String service) {
    SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv()
        .withType(ConstSampler.TYPE)
        .withParam(1);

    ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv();

    Configuration config = new Configuration(service)
        .withSampler(samplerConfig)
        .withReporter(reporterConfig);

    return config.getTracer();
  }

  public static Span startServerSpan(Tracer tracer, HttpHeaders httpHeaders, String operationName) {
    // format the headers for extraction
    MultivaluedMap<String, String> rawHeaders = httpHeaders.getRequestHeaders();
    final HashMap<String, String> headers = new HashMap<>();
    for (String key : rawHeaders.keySet()) {
      headers.put(key, rawHeaders.get(key).get(0));
    }

    Tracer.SpanBuilder spanBuilder;
    try {
      SpanContext parentSpanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
      if (parentSpanCtx == null) {
        spanBuilder = tracer.buildSpan(operationName);
      } else {
        spanBuilder = tracer.buildSpan(operationName).asChildOf(parentSpanCtx);
      }
    } catch (IllegalArgumentException e) {
      spanBuilder = tracer.buildSpan(operationName);
    }
    return appendCustomTags(spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)).start();
  }

  public static Tracer.SpanBuilder appendCustomTags(Tracer.SpanBuilder spanBuilder) {
    return spanBuilder.withTag("env", ENV_TAGS[RAND.nextInt(ENV_TAGS.length)]).
        withTag("location", LOCATION_TAGS[RAND.nextInt(LOCATION_TAGS.length)]).
        withTag("tenant", TENANT_TAGS[RAND.nextInt(TENANT_TAGS.length)]);
  }

}
