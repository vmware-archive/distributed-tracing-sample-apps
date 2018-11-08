package com.wfsample.styling;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import com.wavefront.config.ReportingUtils;
import com.wavefront.config.WavefrontReportingConfig;
import com.wavefront.dropwizard.metrics.DropwizardMetricsReporter;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.jersey.WavefrontJerseyFilter;
import com.wavefront.sdk.jersey.YamlReader;
import com.wfsample.common.BeachShirtsMetricRegistry;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

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
  public JerseyConfig() {

    String applicationTagsYamlFile = "/Projects/wf/hackathon/springboot-jersey-app/styling/src/main/resources/styling-at.yaml";
    String wfReportingConfigYamlFile = "/Projects/wf/hackathon/springboot-jersey-app/common/src/main/resources/wf-reporting-config.yaml";

    // Instantiate the WavefrontJerseyFilter
    WavefrontJerseyFilter wavefrontJerseyFilter =
            YamlReader.constructJerseyFilter(applicationTagsYamlFile, wfReportingConfigYamlFile);
    register(wavefrontJerseyFilter);

    WavefrontReportingConfig wavefrontReportingConfig = ReportingUtils.constructWavefrontReportingConfig(wfReportingConfigYamlFile);
    WavefrontSender wavefrontSender = ReportingUtils.constructWavefrontSender(wavefrontReportingConfig);
    ApplicationTags tags = ReportingUtils.constructApplicationTags(applicationTagsYamlFile);

    DropwizardMetricsReporter dropwizardMetricsReporter = DropwizardMetricsReporter.forRegistry(BeachShirtsMetricRegistry.METRIC_REGISTRY).withApplicationTags(tags).withSource("ajay-mac").build(wavefrontSender);
    dropwizardMetricsReporter.start(1, TimeUnit.MINUTES);

    register(new StylingController(wavefrontJerseyFilter.getTracer()));
  }
}