# Wavefront Hackathon - NodeJS App

This is a sample NodeJS application called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

## Requirements
Node.js version >= 8.9.1 is supported.

### Running the Application Locally
1. Run Jaeger in your env using the [Docker image](https://www.jaegertracing.io/docs/getting-started):
    ```
       $ docker run -d --name jaeger \
         -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
         -p 5775:5775/udp \
         -p 6831:6831/udp \
         -p 6832:6832/udp \
         -p 5778:5778 \
         -p 16686:16686 \
         -p 14268:14268 \
         -p 9411:9411 \
         jaegertracing/all-in-one:latest
    ```

2. git clone this repo and navigate to this dir:

    ```
     git clone https://github.com/wavefrontHQ/hackathon.git 
     cd hackathon/distributed-tracing/node-js-app
    ```

3. Install the dependencies using below commands:
    ```
    npm install
    ```

4. Start the services using below commands:
    ```
    node beachshirt/app.js
    ```

5. Test the service by executing below API:
    ```
    http://localhost:3000/shop/menu
    ```
6. Use ./loadgen.sh in the root directory to send a request of ordering shirts every {interval} seconds. You will see some random failures which are added by us.

7. Now go to Jaeger UI (http://localhost:16686), if you're using all-in-one docker image as given above and look for the traces for service "shopping" and click on Find Traces.

8. Stop loadgen.

## Switching from Jaeger to Wavefront
1. Configure the [Wavefront Jaeger Integration](https://docs.wavefront.com/jaeger.html#jaeger-integration-setup):
   * Install and configure the Wavefront proxy (version >= 4.38), adding `traceJaegerListenerPorts=<enter-available-port>`. You can use the following command to run Wavefront proxy in docker:
       ```
      $ docker run -d --name wfproxy \
         -e WAVEFRONT_URL=https://{CLUSTER}.wavefront.com/api/ \
         -e WAVEFRONT_TOKEN={TOKEN} \
         -e JAVA_HEAP_USAGE=512m \
         -e WAVEFRONT_PROXY_ARGS="--traceListenerPorts 30000 --histogramDistListenerPorts 2878  --traceJaegerListenerPorts 50000" \
         -p 2878:2878 \
         -p 4242:4242 \
         -p 30000:30000 \
         -p 50000:50000 \
         wavefronthq/proxy:latest
       ```
   * Replace `{CLUSTER}` with the cluster name of your wavefront. Replace `{TOKEN}` with the valid API token.
   
   * Restart Jaeger using the following commands:
      ```
      // Stop and remove the Jaeger container.
      $ docker stop jaeger
      $ docker rm jaeger
      // Rerun the Jaeger container.
      $ docker run -d --name jaeger \
         -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
         -p 5775:5775/udp \
         -p 6831:6831/udp \
         -p 6832:6832/udp \
         -p 5778:5778 \
         -p 16686:16686 \
         -p 14268:14268 \
         -p 9411:9411 \
         -e REPORTER_TCHANNEL_HOST_PORT=<wf_proxy_hostname>:<wf_proxy_jaeger_port> \
         -e REPORTER_TYPE=tchannel \
         jaegertracing/all-in-one:latest
      ```
      Replace `<wf_proxy_hostname>` with your Wavefront proxy's hostname, or the IP address of the docker host if the proxy is running on the same host. Replace `<wf_proxy_jaeger_port>` with the port you entered above for `traceJaegerListenerPorts`.

2. Use ./loadgen.sh in the root directory to send a request of ordering shirts every {interval} seconds. You will see some random failures which are added by us.

3. Go to **Applications -> Traces** in the Wavefront UI to visualize your traces. You can also go to **Applications -> Inventory** to visualize the RED metrics that are automatically derived from your tracing spans. Application name is defaulted to `Jaeger`.
