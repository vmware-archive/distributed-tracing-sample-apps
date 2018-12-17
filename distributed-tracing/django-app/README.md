# Wavefront Hackthon - Django App

This is a sample Python application using Django Framework called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

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
   cd hackathon/distributed-tracing/django-app
   ```

4. Run `pip3 install -r requirements.txt` from the root directory of the project (* Note: this sample app is only working with Python 3 since it's built with Django 2, but all the Python SDKs from Wavefront are compatible with both Python 2 and 3).

5. Now run all the services using the commands below:

   ```bash
   python3 ./shopping/manage.py runserver 50050
   python3 ./styling/manage.py runserver 50051
   python3 ./delivery/manage.py runserver 50052
   ```

- Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.
- Now go to Jaeger UI (http://localhost:16686, if you're using all-in-one docker image as given above) and look for the traces for service "shopping" and click on Find Traces.

## Change from Jaeger to Wavefront

1. Install [Wavefront Opentracing SDK](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-python) via `pip`:

   ```bash
   pip3 install wavefront-opentracing-sdk-python
   ```

2. If you are sending tracing spans to Wavefront via Proxy, then make sure you are using proxy version >= v4.33:

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

3. If you are sending data to Wavefront via Direct Ingestion, then make sure you have the cluster name and corresponding token from [https://{cluster}.wavefront.com/settings/profile](https://{cluster}.wavefront.com/settings/profile).

4. Go to `settings.py` of each services and change the value of  `TRACER`  to [WavefrontTracer](https://github.com/wavefrontHQ/wavefront-opentracing-sdk-python#tracer) instead of Jaeger Tracer as follows:

   ```python
   proxy_client = WavefrontProxyClient(
       host="<PROXY_HOST>",
       tracing_port=30000,
       distribution_port=40000,
       metrics_port=2878
   )
   
   proxy_reporter = WavefrontSpanReporter(client=proxy_client)
   
   TRACER = WavefrontTracer(reporter=proxy_reporter) 
   ```

5. Now restart all the services again using below commands from root directory of the project.

   ```bash
   python3 ./shopping/manage.py runserver 50050
   python3 ./styling/manage.py runserver 50051
   python3 ./delivery/manage.py runserver 50052
   ```

6. Generate some load via loadgen - Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds.

7. Now all the metrics, histograms and traces should be sent to Wavefront. Go to the UI and click on Browse -> Applications.