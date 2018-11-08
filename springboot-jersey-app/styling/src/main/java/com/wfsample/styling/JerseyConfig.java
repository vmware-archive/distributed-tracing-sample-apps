package com.wfsample.styling;

import com.wavefront.dropwizard.metrics.DropwizardMetricsReporter;
import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.appagent.jvm.reporter.WavefrontJvmReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.jersey.WavefrontJerseyFilter;
import com.wavefront.sdk.jersey.reporter.WavefrontJerseyReporter;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.opentracing.Tracer;

import static com.wfsample.common.BeachShirtsUtils.METRIC_REGISTRY;
import static com.wfsample.common.BeachShirtsUtils.getSender;

/**
 * Jersey Configuration class for Delivery Service.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

  @Inject
  public JerseyConfig() throws IOException {
    WavefrontSender sender = getSender();
    ApplicationTags tags = new ApplicationTags.Builder("hao-beachshirts",
        "styling").cluster("us-west-1").shard("primary").build();
    WavefrontJerseyReporter jerseyReporter = new WavefrontJerseyReporter.Builder(tags).build(sender);
    WavefrontJvmReporter jvmReporter = new WavefrontJvmReporter.Builder(tags).build(sender);
    WavefrontSpanReporter spanReporter = new WavefrontSpanReporter.Builder().build(sender);
    Tracer tracer = new WavefrontTracer.Builder(spanReporter, tags).build();
    register(new WavefrontJerseyFilter.Builder(jerseyReporter, tags).withTracer(tracer).build());
    jerseyReporter.start();
    jvmReporter.start();
    DropwizardMetricsReporter metricsReporter = DropwizardMetricsReporter.forRegistry(METRIC_REGISTRY).
        withApplicationTags(tags).reportDayDistribution().reportHourDistribution().reportMinuteDistribution().
        build(sender);
    metricsReporter.start(1, TimeUnit.MINUTES);
    register(new StylingController(tracer));
  }

}