package com.wfsample.common;

import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceExporter;
import io.opencensus.exporter.trace.ocagent.OcAgentTraceExporterConfiguration;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.samplers.Samplers;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;


/**
 * Utilities for implementing Opencensus Tracing and Span Context Propagation
 *
 * @author mshvol
 */
public class TraceUtils {

    private static final TextFormat textFormat = Tracing.getPropagationComponent().getTraceContextFormat();

    static class TraceInjectionFilter implements ClientRequestFilter {
        private Tracer tracer;

        private static final TextFormat.Setter contextInjector = new TextFormat.Setter<ClientRequestContext>() {
            @Override
            public void put(ClientRequestContext context, String key, String value) {
                context.getHeaders().add(key,value);
            }
        };

        public TraceInjectionFilter(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        public void filter(ClientRequestContext clientRequestContext) throws IOException {
            textFormat.inject(tracer.getCurrentSpan().getContext(), clientRequestContext, contextInjector);
        }
    }

    private static final TextFormat.Getter headerExtractor = new TextFormat.Getter<HttpHeaders>() {
        @Override
        public String get(HttpHeaders httpHeaders, String key) {
            MultivaluedMap<String, String> rawHeaders = httpHeaders.getRequestHeaders();
            return rawHeaders.getFirst(key);
        }
    };


    public static Scope StartSpan(Tracer tracer, String spanName, HttpHeaders httpHeaders) {
        SpanContext context = null;
        try {
            context = textFormat.extract(httpHeaders, headerExtractor);
        } catch (SpanContextParseException e) {
        }

        if (context == null) {
            return tracer.spanBuilder(spanName).startScopedSpan();
        } else {
            return tracer.spanBuilderWithRemoteParent(spanName, context).startScopedSpan();
        }
    }

    public static void initializeTracing(String endpoint) {
        OcAgentTraceExporter.createAndRegister(
                OcAgentTraceExporterConfiguration.builder()
                        .setEndPoint(endpoint)
                        .setUseInsecure(true)
                        .setEnableConfig(false)
                        .build());

        TraceConfig traceConfig = io.opencensus.trace.Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(
                activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());
    }


}
