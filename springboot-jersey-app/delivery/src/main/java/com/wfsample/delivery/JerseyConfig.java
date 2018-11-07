package com.wfsample.delivery;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import com.codahale.metrics.MetricRegistry;
import com.wavefront.dropwizard.metrics.DropwizardMetricsReporter;
import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.Reporter;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.appagent.jvm.reporter.WavefrontJvmReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.jersey.WavefrontJerseyFilter;
import com.wavefront.sdk.jersey.reporter.WavefrontJerseyReporter;
import com.wavefront.sdk.proxy.WavefrontProxyClient;
import io.opentracing.Tracer;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    String proxyHost = "localhost";

    //1. Set Up Application Tags
    String application = "xtong-BeachShirt";
    String service = "delivery";
    String cluster = "us-west-2";
    String shard = "secondary";

    Map<String, String> customTags = new HashMap<String, String>() {{
      put("location", "Oregon");
      put("env", "Staging");
    }};
    ApplicationTags applicationTags = new ApplicationTags.Builder(application, service).
            cluster(cluster).       // optional
            shard(shard).           // optional
            customTags(customTags). // optional
            build();

    //2. Set Up a WavefrontSender
    WavefrontProxyClient.Builder proxyClientBuilder = new WavefrontProxyClient.Builder(proxyHost);
    proxyClientBuilder.metricsPort(2878);
    proxyClientBuilder.distributionPort(40000);
    proxyClientBuilder.tracingPort(30000);
    WavefrontSender wavefrontSender = proxyClientBuilder.build();

    //2.b Create a Builder and configure a DropwizardMetricsReporter
    MetricRegistry metricRegistry = new MetricRegistry();
    DropwizardMetricsReporter.Builder dropwizardMetricsBuilder = DropwizardMetricsReporter.forRegistry(metricRegistry);
    dropwizardMetricsBuilder.withSource("xtong-delivery-source");
    dropwizardMetricsBuilder.withReporterPointTag("env", "Staging");
    dropwizardMetricsBuilder.withReporterPointTag("location", "Oregon");
    dropwizardMetricsBuilder.withApplicationTags(applicationTags);
    dropwizardMetricsBuilder.prefixedWith("xtong");
    dropwizardMetricsBuilder.withJvmMetrics();
    dropwizardMetricsBuilder.reportMinuteDistribution();
    dropwizardMetricsBuilder.reportHourDistribution();
    dropwizardMetricsBuilder.reportDayDistribution();

    //3. Create a WavefrontJerseyReporter
    WavefrontJerseyReporter.Builder jerseyReporterBuilder = new WavefrontJerseyReporter.Builder(applicationTags);
    jerseyReporterBuilder.withSource("xtong-delivery-source");
    jerseyReporterBuilder.reportingIntervalSeconds(30);
    WavefrontJerseyReporter wfJerseyReporter = jerseyReporterBuilder.build(wavefrontSender);

    //3.b
    WavefrontJvmReporter.Builder wfJvmReporterBuilder = new WavefrontJvmReporter.Builder(applicationTags);
    wfJvmReporterBuilder.withSource("xtong-delivery-source");
    wfJvmReporterBuilder.reportingIntervalSeconds(30);
    WavefrontJvmReporter wfJvmReporter = wfJvmReporterBuilder.build(wavefrontSender);

    //3.c Create a DropwizardMetricsReporter
    DropwizardMetricsReporter dropwizardMetricsReporter = dropwizardMetricsBuilder.build(wavefrontSender);


    //4. WavefrontTracer
    Reporter wfSpanReporter = new WavefrontSpanReporter.Builder().
            withSource("xtong-delivery-source"). // optional nondefault source name
            build(wavefrontSender);
    int totalFailures = wfSpanReporter.getFailureCount();
    Tracer tracer = new WavefrontTracer.Builder(wfSpanReporter, applicationTags).build();


    //5. Create WavefrontJerseyFilter
    WavefrontJerseyFilter.Builder wfJerseyFilterBuilder =
            new WavefrontJerseyFilter.Builder(wfJerseyReporter, applicationTags);
    wfJerseyFilterBuilder.withTracer(tracer);
    WavefrontJerseyFilter wfJerseyFilter = wfJerseyFilterBuilder.build();


    //6. Register the WavefrontJerseyFilter
    register(wfJerseyFilter);
    // Start the reporter
    wfJerseyReporter.start();
    wfJvmReporter.start();
    dropwizardMetricsReporter.start(30, TimeUnit.SECONDS);

    register(new DeliveryController());
  }

}