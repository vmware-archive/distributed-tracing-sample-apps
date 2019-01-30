# Wavefront Hackathon - Go App

This is a sample Go application using `chi` router framework called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

## Running the Application Locally

1. `git clone` this repo and navigate to this dir:
    ```bash
    git clone https://github.com/wavefrontHQ/hackathon.git
    cd hackathon/3D-microservices-observability/go-chi/
    ```
2. Build the binary for services:
    ```bash
    go build -o target/beachshirts main/beachshirts.go
    ```
    **Note**: Requires Go 1.11

3. Run all the services using the commands below:
    ```bash
    ./target/beachshirts conf/shopping.conf
    ./target/beachshirts conf/styling.conf
    ./target/beachshirts conf/delivery.conf
    ```

4. Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.

    - You can view the shopping menu with a HTTP GET request: `http://localhost:50050/shop/menu`
    - You can also Order Shirts using a HTTP POST:
        ```
        http://localhost:50050/shop/order
        Payload: {"styleName" : "testStyle1","quantity" : 5}
        ```

<br/>

## 3D Microservices Observability (Out of the box metrics, histograms and traces) setup

> TODO