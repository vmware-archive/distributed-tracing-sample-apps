# Wavefront Hackathon - ASP.NET Core App

This is a sample .NET Core application called BeachShirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

## Running the Application Locally

1. `git clone` this repo and navigate to this dir:

2. ```bash
   git clone --single-branch --branch han/training https://github.com/wavefrontHQ/hackathon.git hackathon-aspnetcore
   cd hackathon-aspnetcore/Wavefront-DT/aspnetcore-app
   ```

3. Now run all the services using the commands below:

   ```bash
   dotnet run --project src/BeachShirts.Shopping/BeachShirts.Shopping.csproj
   dotnet run --project src/BeachShirts.Styling/BeachShirts.Styling.csproj
   dotnet run --project src/BeachShirts.Delivery/BeachShirts.Delivery.csproj
   ```

4. Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.

5. This app is instrumented with a NoopTracer. Next step is to switch from NoopTracer to WavefrontTracer to see the traces emitted from beachshirts application to Wavefront.

## Use Wavefront Tracer

1. Add the following dependency [Wavefront.OpenTracing.SDK.CSharp](https://www.nuget.org/packages/Wavefront.OpenTracing.SDK.CSharp/) to the `BeachShirts.Common` project.

   ```bash
   dotnet add src/BeachShirts.Common/BeachShirts.Common.csproj package Wavefront.OpenTracing.SDK.CSharp
   ```

2. Open your preferred IDE and add the following imports to `aspnetcore-app/src/BeachShirts.Common/Tracing.cs`:

   ```csharp
   using Wavefront.OpenTracing.SDK.CSharp;
   using Wavefront.OpenTracing.SDK.CSharp.Reporting;
   using Wavefront.SDK.CSharp.Common.Application;
   using Wavefront.SDK.CSharp.DirectIngestion;
   using Wavefront.SDK.CSharp.Proxy;
   ```

3. You can send data to Wavefront either using one of the 2 options below:

   * **Option A** - via Direct Ingestion

   * **Option B** - via Wavefront Proxy

**Option A** - If you are sending data to Wavefront via Direct Ingestion, then make sure you have the cluster name and corresponding token from [https://{cluster}.wavefront.com/settings/profile](https://{cluster}.wavefront.com/settings/profile).

Now go to `aspnetcore-app/src/BeachShirts.Common/Tracing.cs` and change the `ITracer Init(string service)` method to return a [WavefrontTracer](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-csharp#set-up-a-tracer) as follows:

   ```csharp
   public static ITracer Init(string service)
   {
       // TODO: Replace {cluster} with your wavefront URL and obtain wavefront API token
       var wfDirectIngestionClientBuilder = new WavefrontDirectIngestionClient.Builder(
           "https://{cluster}.wavefront.com", "{wf_api_token}");
       var wavefrontSender = wfDirectIngestionClientBuilder.Build();
       /*
        * TODO: You need to assign your microservices application a name.
        * For this hackathon, please prepend your name (example: "John") to the BeachShirts application,
        * for example: applicationName = "John-BeachShirts"
        */
       var applicationTags = new ApplicationTags.Builder(applicationName, service).Build();
       var wfSpanReporter = new WavefrontSpanReporter.Builder().Build(wavefrontSender);
       var wfTracerBuilder = new WavefrontTracer.Builder(wfSpanReporter, applicationTags);
       return wfTracerBuilder.Build();
   }
   ```

**Option B** - If you are sending tracing spans to Wavefront via Proxy, then make sure you are using proxy version >= v4.36:

   * See [here](https://docs.wavefront.com/proxies_installing.html#proxy-installation) for details on installing the Wavefront proxy.

   * Enable `traceListenerPorts` on the Wavefront proxy configuration: See [here](https://docs.wavefront.com/proxies_configuring.html#proxy-configuration-properties) for details.

      **Note**: You need to use the same tracing port (for example: `30000`) when you instantiate the `WavefrontProxyClient` (see below).

   * You can use following command to run Wavefront proxy in docker:

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

Now go to `aspnetcore-app/src/BeachShirts.Common/Tracing.cs` and change the `ITracer Init(string service)` method to return a [WavefrontTracer](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-csharp#set-up-a-tracer) as follows:

   ```csharp
   public static ITracer Init(string service)
   {
       var wfProxyClientBuilder = new WavefrontProxyClient.Builder("localhost")
           .MetricsPort(2878).TracingPort(30000).DistributionPort(40000);
       var wavefrontSender = wfProxyClientBuilder.Build();
       /*
        * TODO: You need to assign your microservices application a name.
        * For this hackathon, please prepend your name (example: "John") to the BeachShirts application,
        * for example: applicationName = "John-BeachShirts"
        */
       var applicationTags = new ApplicationTags.Builder(applicationName, service).Build();
       var wfSpanReporter = new WavefrontSpanReporter.Builder().Build(wavefrontSender);
       var wfTracerBuilder = new WavefrontTracer.Builder(wfSpanReporter, applicationTags);
       return wfTracerBuilder.Build();
   }
   ```

4. Now restart all the services again using below commands from root directory of the project.

```bash
dotnet run --project src/BeachShirts.Shopping/BeachShirts.Shopping.csproj
dotnet run --project src/BeachShirts.Styling/BeachShirts.Styling.csproj
dotnet run --project src/BeachShirts.Delivery/BeachShirts.Delivery.csproj
```

5. Generate some load via loadgen - Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds.

6. Go to **Applications -> Traces** in the Wavefront UI to visualize your traces. You can also go to **Applications -> Inventory** to visualize the RED metrics that are automatically derived from your tracing spans.


# Add an Additional Span

1. There is a `HandleDispatchAsync` method in `aspnetcore-app/src/BeachShirts.Delivery/Controllers/DeliveryController.cs` that has yet to be instrumented:

```csharp
private async Task HandleDispatchAsync()
{
    await Task.Delay(TimeSpan.FromSeconds(1 + random.NextDouble() / 3));
}
```

Let's add a span for this method.

2. Update the method to be:

```csharp
private async Task HandleDispatchAsync(ISpanContext context)
{
    using (var scope = tracer.BuildSpan("HandleDispatchAsync")
            .AddReference(References.ChildOf, context)
            .StartActive())
    {
        await Task.Delay(TimeSpan.FromSeconds(1 + random.NextDouble() / 3));
    }
}
```

3. Change the method call from `Task.Run(async () => await HandleDispatchAsync());` to `Task.Run(async () => await HandleDispatchAsync(tracer.ActiveSpan.Context));`

4. Now restart all the services again using below commands from root directory of the project.

```bash
dotnet run --project src/BeachShirts.Shopping/BeachShirts.Shopping.csproj
dotnet run --project src/BeachShirts.Styling/BeachShirts.Styling.csproj
dotnet run --project src/BeachShirts.Delivery/BeachShirts.Delivery.csproj
```

5. Generate some load via loadgen - Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds.

6. Go to **Applications -> Traces** in the Wavefront UI to visualize your traces. You can also go to **Applications -> Inventory** to visualize the RED metrics that are automatically derived from your tracing spans. You should see spans and metrics for `HandleDispatchAsync` now.

