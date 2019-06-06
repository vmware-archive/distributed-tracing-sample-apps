# Wavefront Hackthon - Django App

This is a sample Python application using Django Framework called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

## Running the Application Locally
1. `git clone` this repo and navigate to this dir:

   ```bash
   git clone https://github.com/wavefrontHQ/hackathon.git
   cd hackathon/Wavefront-DT/django-app
   ```

2. Run `pip3 install -r requirements.txt` from the root directory of the project (* **Note**: This sample app works only with Python 3 since it’s built with Django 2, but all the Wavefront Python SDKs are compatible with both Python 2 and 3).

3. Now run all the services using the commands below:

   ```bash
   python3 ./shopping/manage.py runserver 50050
   python3 ./styling/manage.py runserver 50051
   python3 ./delivery/manage.py runserver 50052
   ```

4. Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.

5. This app is instrumented with a NoopTracer. Next step is to switch from NoopTracer to WavefrontTracer to see the traces emitted from beachshirts application to Wavefront.

## Use Wavefront Tracer

1. Install [Wavefront Opentracing SDK](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-python) via `pip`:

   ```bash
   pip3 install wavefront-opentracing-sdk-python
   ```

2. You can send data to Wavefront either using one of the 2 options below:

   * **Option A** - via Direct Ingestion
   
   * **Option B** - via Wavefront Proxy

**Option A** - If you are sending data to Wavefront via Direct Ingestion, then make sure you have the cluster name and corresponding token from [https://{cluster}.wavefront.com/settings/profile](https://{cluster}.wavefront.com/settings/profile).

Go to `settings.py` of each services and change the value of  `TRACER`  to [WavefrontTracer](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-python#tracer) instead of Jaeger Tracer as follows:

   ```python
   # Doc for Instantiating ApplicationTags:
   # https://github.com/wavefrontHQ/wavefront-sdk-python/blob/master/docs/apptags.md

   application_tags = ApplicationTags(application="<APP_NAME>",
                                      service="<SERVICE_NAME>",
                                      cluster="<CLUSTER_NAME>",
                                      shard="<SHARD_NAME>")

   direct_client = WavefrontDirectClient(
    server="<SERVER_ADDR>",
    token="<TOKEN>"
   )

   direct_reporter = WavefrontSpanReporter(client=direct_client)

   TRACER = WavefrontTracer(reporter=direct_reporter, application_tags=application_tags)
   ```


**Option B** - If you are sending tracing spans to Wavefront via Proxy, then make sure you are using proxy version >= v4.33:

   - See [here](https://docs.wavefront.com/proxies_installing.html#proxy-installation) for details on installing the Wavefront proxy.

   - Enable `traceListenerPorts` on the Wavefront proxy configuration: See [here](https://docs.wavefront.com/proxies_configuring.html#proxy-configuration-properties) for details.

     **Note**: You need to use the same tracing port (for example: `30000`) when you instantiate the `WavefrontProxyClient` (see below).

   - You can use following command to run Wavefront proxy in docker:

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

- Go to `settings.py` of each services and change the value of  `TRACER`  to [WavefrontTracer](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-python#tracer) instead of Jaeger Tracer as follows:

   ```python
   # Doc for Instantiating ApplicationTags:
   # https://github.com/wavefrontHQ/wavefront-sdk-python/blob/master/docs/apptags.md

   application_tags = ApplicationTags(application="<APP_NAME>",
                                      service="<SERVICE_NAME>",
                                      cluster="<CLUSTER_NAME>",
                                      shard="<SHARD_NAME>")

   proxy_client = WavefrontProxyClient(
       host="<PROXY_HOST>",
       tracing_port=30000,
       distribution_port=40000,
       metrics_port=2878
   )

   proxy_reporter = WavefrontSpanReporter(client=proxy_client)

   TRACER = WavefrontTracer(reporter=proxy_reporter, application_tags=application_tags)
   ```
   
3. Now restart all the services again using below commands from root directory of the project.

   ```bash
   python3 ./shopping/manage.py runserver 50050
   python3 ./styling/manage.py runserver 50051
   python3 ./delivery/manage.py runserver 50052
   ```

4. Generate some load via loadgen - Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds.

5. Go to **Applications -> Traces** in the Wavefront UI to visualize your traces. You can also go to **Applications -> Inventory** to visualize the RED metrics that are automatically derived from your tracing spans.
