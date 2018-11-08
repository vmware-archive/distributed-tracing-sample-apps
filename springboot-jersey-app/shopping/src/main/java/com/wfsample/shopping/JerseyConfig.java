package com.wfsample.shopping;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import com.wavefront.config.ReportingUtils;
import com.wavefront.config.WavefrontReportingConfig;
import com.wavefront.dropwizard.metrics.DropwizardMetricsReporter;
import com.wavefront.sdk.common.WavefrontSender;
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

    String applicationTagsYamlFile = "/Projects/wf/hackathon/springboot-jersey-app/shopping/src/main/resources/shopping-at.yaml";
    String wfReportingConfigYamlFile = "/Projects/wf/hackathon/springboot-jersey-app/common/src/main/resources/wf-reporting-config.yaml";

    // Instantiate the WavefrontJerseyFilter
    WavefrontJerseyFilter wavefrontJerseyFilter =
            YamlReader.constructJerseyFilter(applicationTagsYamlFile, wfReportingConfigYamlFile);
    register(wavefrontJerseyFilter);

    register(new ShoppingController(wavefrontJerseyFilter.getTracer()));
  }
}