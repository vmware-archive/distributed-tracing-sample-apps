# Wavefront Hackthon - Dropwizard App

This is a sample java application using Dropwizard called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for beach. 


## Running Application locally 

- Run `mvn clean install` from the root directory of the project.

- Now run all the services using below commands from root directory of the project.

  ```bash
  java -jar ./shopping/target/shopping-1.0-SNAPSHOT.jar server ./shopping/app.yaml
  java -jar ./styling/target/styling-1.0-SNAPSHOT.jar server ./styling/app.yaml
  java -jar ./delivery/target/delivery-1.0-SNAPSHOT.jar server ./delivery/app.yaml
  ```

- Now view the shopping menus using HTTP GET request: `http://localhost:50050/shop/menu`

- Order shirts using HTTP POST request:

- ```bash
  http://localhost:50050/shop/order
  Payload: {"styleName" : "testStyle1","quantity" : 5}
  ```

- Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds


## 3D Microservices Observability (Out of the box metrics, histograms and traces) setup

In order to instrument your application to emit out of the box metrics, histograms and traces follow the steps listed below.

1. Add following dependency of [`Wavefront Jersey SDK`](https://github.com/wavefrontHQ/wavefront-jersey-sdk-java) to the `pom.xml`

```xml
<dependencies>
  ...
  <dependency>
    <groupId>com.wavefront</groupId>
    <artifactId>wavefront-jersey-sdk-java</artifactId>
    <version>${latest}</version>
  </dependency>
  ...
</dependencies> 
```

2. If you are sending data to Wavefront via Proxy, then make sure you are using proxy version >= v4.34:
   * See [here](https://docs.wavefront.com/proxies_installing.html#proxy-installation) for details on installing the Wavefront proxy.
   * Enable [`histogramDistListenerPorts`](https://docs.wavefront.com/proxies_histograms.html) and [`traceListenerPorts`](https://docs.wavefront.com/proxies_configuring.html#proxy-configuration-properties).

      **Note**: You need to use the same histogram port (for example: `40000`) and the same tracing port (for example: `30000`) when you instantiate the `WavefrontProxyClient` (see below).

   * Command for running in Wavefront Proxy in [Docker](https://docs.docker.com/install/):

   ```bash
   docker run -d \
       -e WAVEFRONT_URL=https://{CLUSTER}.wavefront.com/api/ \
       -e WAVEFRONT_TOKEN={TOKEN} \
       -e JAVA_HEAP_USAGE=512m \
       -e WAVEFRONT_PROXY_ARGS="--traceListenerPorts 30000 --histogramDistListenerPorts 40000" \
       -p 2878:2878 \
       -p 4242:4242 \
       -p 30000:30000 \
       -p 40000:40000 \
       wavefronthq/proxy:latest
   ```

3. If you are sending data to Wavefront via Direct Ingestion, then make sure you have the cluster name and corresponding token from [https://{cluster}.wavefront.com/settings/profile](https://{cluster}.wavefront.com/settings/profile).

4. Your sample app has 3 microservices - `shopping`, `styling` and `delivery`.

   * You need to define ApplicationTags for all the 3 microservices. ApplicationTags are composed of application name and service name. Optionally, you can also define the cluster in which the service is running along with what shard it is running on. You can also define optional custom tags.

  * shopping yaml file - `dropwizard-app/shopping/src/main/resources/shopping.yaml`
```yaml
# For this hackathon, please prepend your name (example: "john") to the beachshirts application,
# for example: applicationName = "john-beachshirts"
application: <your-name>-beachshirts
cluster: us-west-1
service: shopping
shard: primary
customTags:
  location: Palo-Alto
  env: Staging
```

  * styling yaml file - `dropwizard-app/styling/src/main/resources/styling.yaml`
```yaml
# For this hackathon, please prepend your name (example: "john") to the beachshirts application,
# for example: applicationName = "john-beachshirts"
application: <your-name>-beachshirts
cluster: us-west-1
service: styling
shard: primary
customTags:
  location: Palo-Alto
  env: Staging
```

  * delivery yaml file - ``dropwizard-app/delivery/src/main/resources/delivery.yaml``
```yaml
# For this hackathon, please prepend your name (example: "john") to the beachshirts application,
# for example: applicationName = "john-beachshirts"
application: <your-name>-beachshirts
cluster: us-west-1
service: delivery
shard: primary
customTags:
  location: Palo-Alto
  env: Staging
```

5. Next, use the [Wavefront Jersey SDK Quickstart](https://github.com/wavefrontHQ/wavefront-jersey-sdk-java#quickstart) to instantiate WavefrontJerseyFilter. 
   * You have already created the ApplicationTags in the step above. 
   * Also, if you are using Wavefront proxy to send data to Wavefront then use proxy version >= 4.34
   * You need to create 2 YAML files per microservice: `wf-reporting-config.yaml` and applicationTags YAML file highlighted in the step above.

Since all the 3 microservices (shopping, styling and delivery) in the sample app are based on Jersey Framework, you need to instantiate WavefrontJerseyFilter for every microservice.
You can do this using the below sample code -
```java
// Instantiate the WavefrontJerseyFilter
WavefrontJerseyFactory wfJerseyFactory = new WavefrontJerseyFactory(
    applicationTagsYamlFile, wfReportingConfigYamlFile);
WavefrontJerseyFilter wfJerseyFilter = 
    wfJerseyFactory.getWavefrontJerseyFilter();
WavefrontJaxrsClientFilter wfJaxrsClientFilter = 
    wfJerseyFactory.getWavefrontJaxrsClientFilter();
```

6. After instantiating WavefrontJerseyFilter for the 3 microservices, you need to register it with the respective microservice. You need to paste the below snippet for
  * dropwizard-app/shopping/src/main/java/com/wfsample/shopping/ShoppingService.java
  * dropwizard-app/styling/src/main/java/com/wfsample/styling/StylingService.java
  * dropwizard-app/delivery/src/main/java/com/wfsample/delivery/DeliveryService.java
  
```java
// Example on how to register
public class MyApplication extends Application<MyConfiguration>{
    @Override
    public void run(MyApplicationConfiguration configuration, Environment environment) {
        ...
        ...
        // Create the filter 
        WavefrontJerseyFilter wfJerseyFilter = buildJerseyFilter();  // pseudocode

        // Register the filter with Dropwizard Jersey environment
        environment.jersey().register(wfJerseyFilter);
        ...
        ...
    }
}
```

Click [here](https://github.com/wavefrontHQ/wavefront-jersey-sdk-java/blob/master/docs/dropwizard.md) for more details on register WavefrontJerseyFilter with your dropwizard application microservice.

7. In order for traces to be collected across microservices process boundaries, we need to correctly propagate context.
We will rely on [WavefrontJaxrsClientFilter](https://github.com/wavefrontHQ/wavefront-jaxrs-sdk-java#wavefrontjaxrsclientfilter) to do this.

You need to pass WavefrontJaxrsClientFilter that was instantiated in the step 4 above to `BeachShirtsUtils.createProxyClient` method in -
   * [ShoppingService](https://github.com/wavefrontHQ/hackathon/blob/master/3D-microservices-observability/dropwizard-app/shopping/src/main/java/com/wfsample/shopping/ShoppingService.java)
   * [StylingService](https://github.com/wavefrontHQ/hackathon/blob/master/3D-microservices-observability/dropwizard-app/styling/src/main/java/com/wfsample/styling/StylingService.java)

```java
// Example of shopping/../ShoppingController  
ShoppingController(WavefrontJaxrsClientFilter wfJaxrsClientFilter) {
  String stylingUrl = "http://localhost:50051";
  WavefrontJaxrsClientFilter wavefrontJaxrsFilter = null;
  /**
   * TODO: Initialize WavefrontJaxrsClientFilter in shopping/../JerseyConfig.java
   */
  wavefrontJaxrsFilter = wfJaxrsClientFilter;
  this.stylingApi = BeachShirtsUtils.createProxyClient(stylingUrl, StylingApi.class, wavefrontJaxrsFilter);
}
```

8. After making all the code changes, run `mvn clean install` from the root directory of the project.

9. Now restart all the services again using below commands from root directory of the project.

  ```bash
  java -jar ./shopping/target/shopping-1.0-SNAPSHOT.jar
  java -jar ./styling/target/styling-1.0-SNAPSHOT.jar
  java -jar ./delivery/target/delivery-1.0-SNAPSHOT.jar
  ```

10. Generate some load via loadgen - Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds.

11. Now all the metrics, histograms and traces should be sent to Wavefront. Go to the UI and click on Browse -> Applications.

12. (Optional) Custom Business Metrics - There are several `#TODO` in the code to address the custom business metrics. Once those TODOs are completed using the [Wavefront Dropwizard Metrics SDK](https://github.com/wavefrontHQ/wavefront-dropwizard-metrics-sdk-java), you can view those metrics on the Wavefront UI.
