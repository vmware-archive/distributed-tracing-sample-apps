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
         jaegertracing/all-in-one:1.8
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