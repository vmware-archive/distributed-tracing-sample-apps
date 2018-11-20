# Wavefront Hackthon - Dropwizard App

This is a sample Java application using Dropwizard called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

## Running the Application Locally

1. Run Jaeger in your env using the [Docker image](https://www.jaegertracing.io/docs/getting-started):

   ```bash
   $ docker run -d --name jaeger \
     -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
     -p 5775:5775/udp \
     -p 6831:6831/udp \
     -p 6832:6832/udp \
     -p 5778:5778 \
     -p 16686:16686 \
     -p 14268:14268 \
     -p 9411:9411 \
     jaegertracing/all-in-one:1.8
   ```

2. Run `mvn clean install` from the root directory of the project.

3. Now run all the services using the commands below:

   ```bash
   java -jar ./shopping/target/shopping-1.0-SNAPSHOT.jar server ./shopping/app.yaml
   java -jar ./styling/target/styling-1.0-SNAPSHOT.jar server ./styling/app.yaml
   java -jar ./delivery/target/delivery-1.0-SNAPSHOT.jar server ./delivery/app.yaml
   ```

- Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.
- Now go to Jaeger UI (http://localhost:16686, if you're using all-in-one docker image as given above) and look for the traces for service "shopping" and click on Find Traces.

## Change from Jaeger to Wavefront

1. Add dependency of `Wavefront Opentracing SDK`:

```xml
  <dependency>
    <groupId>com.wavefront</groupId>
    <artifactId>wavefront-opentracing-sdk-java</artifactId>
    <version>0.9.1</version>
  </dependency>
```

2. If you sending the tracing spans to Wavefront via Proxy, then make sure you are using proxy version >= v4.32
You can find the latest proxy release here - https://packagecloud.io/wavefront/proxy

3. Go to `dropwizard-app/common/../Tracing.java` and change the `Tracer init(String service)` method to return a [WavefrontTracer](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-java#set-up-a-tracer) instead of `com.uber.jaeger.Tracer` as follows:

```java
public static Tracer init(String service) throws IOException {
    WavefrontProxyClient.Builder wfProxyClientBuilder = new WavefrontProxyClient.
        Builder("localhost").metricsPort(2878).tracingPort(30000);
    WavefrontSender wavefrontSender = wfProxyClientBuilder.build();
    ApplicationTags applicationTags = new ApplicationTags.Builder("beachshirts",
        service).build();
    Reporter wfSpanReporter = new WavefrontSpanReporter.Builder().
        withSource("wavefront-tracing-example").build(wavefrontSender);
    WavefrontTracer.Builder wfTracerBuilder = new WavefrontTracer.
        Builder(wfSpanReporter, applicationTags);
    return wfTracerBuilder.build();
}
```

4. Now all the tracing data should be sent to Wavefront. Go to the UI and click on Browse -> Traces
