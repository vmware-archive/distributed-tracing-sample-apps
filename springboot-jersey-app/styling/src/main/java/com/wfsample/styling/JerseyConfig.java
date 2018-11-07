package com.wfsample.styling;

import com.wavefront.dropwizard.metrics.DropwizardMetricsReporter;
import com.wavefront.opentracing.WavefrontTracer;
import com.wavefront.opentracing.reporting.WavefrontSpanReporter;
import com.wavefront.sdk.appagent.jvm.reporter.WavefrontJvmReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.jersey.WavefrontJerseyFilter;
import com.wavefront.sdk.jersey.reporter.WavefrontJerseyReporter;
import com.wfsample.constants.WavefrontConnection;
import io.opentracing.Tracer;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.wfsample.constants.CommonRegistry.METRIC_REGISTRY;

/**
 * Jersey Configuration class for Styling Service.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    @Inject
    public JerseyConfig() throws IOException {
        WavefrontSender sender = WavefrontConnection.getSender();
        ApplicationTags tags = new ApplicationTags.Builder("jbau-wfsample", "styling")
                .build();
        WavefrontJerseyReporter reporter = new WavefrontJerseyReporter.Builder(tags).build(sender);
        WavefrontJvmReporter jvmReporter = new WavefrontJvmReporter.Builder(tags).build(sender);

        WavefrontSpanReporter spanReporter = new WavefrontSpanReporter.Builder().build(sender);
        Tracer tracer = new WavefrontTracer.Builder(spanReporter, tags).build();
        WavefrontJerseyFilter wfjf = new WavefrontJerseyFilter.Builder(reporter, tags).withTracer(tracer).build();
        register(wfjf);
        reporter.start();
        jvmReporter.start();
        DropwizardMetricsReporter metricsReporter = DropwizardMetricsReporter.forRegistry(METRIC_REGISTRY).
                withApplicationTags(tags).reportDayDistribution().reportHourDistribution().reportMinuteDistribution().
                build(sender);

        metricsReporter.start(1, TimeUnit.MINUTES);

        register(new StylingController(tracer));
    }

}