# Wavefront Hackathon - ASP.NET Core App

This is a sample .NET Core application called BeachShirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

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

2. `git clone` this repo and navigate to this dir:

3. ```bash
   git clone https://github.com/wavefrontHQ/hackathon.git
   cd hackathon/distributed-tracing/aspnetcore-app
   ```

4. Now run all the services from the root directory of the project using the commands below:

   ```bash
   dotnet run --project src/BeachShirts.Shopping/BeachShirts.Shopping.csproj
   dotnet run --project src/BeachShirts.Styling/BeachShirts.Styling.csproj
   dotnet run --project src/BeachShirts.Delivery/BeachShirts.Delivery.csproj
   ```

5. Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.

6. Now go to Jaeger UI (http://localhost:16686, if you're using all-in-one docker image as given above) and look for the traces for service "Shopping" and click on Find Traces.

## Change from Jaeger to Wavefront

1. Add [Wavefront.OpenTracing.SDK.CSharp](https://www.nuget.org/packages/Wavefront.OpenTracing.SDK.CSharp/) as a dependency to the BeachShirts.Common project.

   ```bash
   dotnet add src/BeachShirts.Common/BeachShirts.Common.csproj package Wavefront.OpenTracing.SDK.CSharp
   ```

2. Make sure you have the Wavefront proxy (version >= v4.34) installed.

   * See [here](https://docs.wavefront.com/proxies_installing.html#proxy-installation) for details on installing the Wavefront proxy.

   * Enable `traceListenerPorts` on the Wavefront proxy configuration: See [here](https://docs.wavefront.com/proxies_configuring.html#proxy-configuration-properties) for details.

      **Note**: You need to use the same tracing port (for example: `30000`) when you instantiate the `WavefrontProxyClient` (see below).

   * You can use following command to run Wavefront proxy in docker:

   * ```bash
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

4. Go to `src/BeachShirts.Common/Tracing.cs` and change the `ITracer Init(string service)` method to return a [WavefrontTracer](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-csharp#set-up-a-tracer) instead of a Jaeger Tracer as follows:

   ```csharp
   public static ITracer Init(string service)
   {
       var wfProxyClientBuilder = new WavefrontProxyClient.Builder("localhost")
           .MetricsPort(2878).TracingPort(30000);
       var wavefrontSender = wfProxyClientBuilder.Build();
       /*
        * TODO: You need to assign your microservices application a name.
        * For this hackathon, please prepend your name (example: "John") to the BeachShirts application,
        * for example: applicationName = "John-BeachShirts"
        */
       var applicationTags = new ApplicationTags.Builder(applicationName, service).Build();
       var wfSpanReporter = new WavefrontSpanReporter.Builder().
           WithSource("wavefront-tracing-example").Build(wavefrontSender);
       var wfTracerBuilder = new WavefrontTracer.Builder(wfSpanReporter, applicationTags);
       return wfTracerBuilder.Build();
   }
   ```

5. Now restart all the services again using below commands from root directory of the project.

   ```bash
   dotnet run --project src/BeachShirts.Shopping/BeachShirts.Shopping.csproj
   dotnet run --project src/BeachShirts.Styling/BeachShirts.Styling.csproj
   dotnet run --project src/BeachShirts.Delivery/BeachShirts.Delivery.csproj
   ```
   
6. Generate some load via loadgen - Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds.

7. Now all the traces should be sent to Wavefront. Go to the UI and click on Browse -> Traces.
