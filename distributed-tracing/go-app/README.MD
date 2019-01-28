# Wavefront Hackathon - Go App

This is a sample Go application using `chi` router framework called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

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
     jaegertracing/all-in-one:1.9
   ```

2. `git clone` this repo and navigate to this dir:
    ```bash
    git clone https://github.com/wavefrontHQ/hackathon.git
    cd hackathon/3D-microservices-observability/go-chi/
    ```

3. Build the binary for services:
    ```bash
    go build -o target/beachshirts main/beachshirts.go
    ```
    **Note**: Requires Go 1.11

4. Run all the services using the commands below:
    ```bash
    ./target/beachshirts conf/shopping.conf
    ./target/beachshirts conf/styling.conf
    ./target/beachshirts conf/delivery.conf
    ```

5. Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.

    - You can view the shopping menu with a HTTP GET request: `http://localhost:50050/shop/menu`
    - You can also Order Shirts using a HTTP POST:
        ```
        http://localhost:50050/shop/order
        Payload: {"styleName" : "testStyle1","quantity" : 5}
        ```

6. Now go to Jaeger UI (http://localhost:16686, if you're using all-in-one docker image as given above) and look for the traces for service "shopping" and click on Find Traces.

<br/>

## Change from Jaeger to Wavefront

> TODO