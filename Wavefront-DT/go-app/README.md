# Wavefront Hackathon - Go App

This is a sample Go application using `chi` router framework called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for the beach.

## Running the Application Locally

1. `git clone` this repo and navigate to this dir:
    ```bash
    git clone https://github.com/wavefrontHQ/hackathon.git
    cd hackathon/Wavefront-DT/go-app/
    ```

2. Build the binary for services:
    ```bash
    go build -o target/beachshirts  cmd/beachshirts/main.go
    ```
    **Note**: Requires Go 1.11 and above.

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

5. This app is instrumented with a NoopTracer. Next step is to switch from NoopTracer to WavefrontTracer to see the traces emitted from beachshirts application to Wavefront.

<br/>

## Use Wavefront Tracer

1. You can send data to Wavefront using either:
  * **A.** Direct Ingestion
  * **B.** Wavefront Proxy

  ### A. Direct Ingestion

  * Make sure you have the cluster name and API token from [https://{cluster}.wavefront.com/settings/profile](https://{cluster}.wavefront.com/settings/profile).

  * Add the following imports and code changes (replace function `NewGlobalTracer()`) in `tracing.go`

      ```go
      import (
          wfreporter "github.com/wavefronthq/wavefront-opentracing-sdk-go/reporter"
          wftracer "github.com/wavefronthq/wavefront-opentracing-sdk-go/tracer"
          application "github.com/wavefronthq/wavefront-sdk-go/application"
          wavefront "github.com/wavefronthq/wavefront-sdk-go/senders"
      )

      func NewGlobalTracer(serviceName string) io.Closer {

          config := &wavefront.DirectConfiguration{
              Server: "https://{CLUSTER}.wavefront.com",
              Token:  "{TOKEN}",
          }
          sender, err := wavefront.NewDirectSender(config)
          if err != nil {
              log.Printf("Couldn't create Wavefront Sender: %s\n", err.Error())
              os.Exit(1)
          }

          appTags := application.New("beachshirts", serviceName)

          directrep := wfreporter.New(sender, appTags)
          consolerep := wfreporter.NewConsoleSpanReporter(serviceName)

          reporter := wfreporter.NewCompositeSpanReporter(directrep, consolerep)

          tracer := wftracer.New(reporter)

          opentracing.SetGlobalTracer(tracer)

          return ioutil.NopCloser(nil)

      }
      ```

  ### B. Wavefront Proxy

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

    * Add the following imports and code changes (replace function `NewGlobalTracer()`) in `tracing.go`

        ```go
        import (
            wfreporter "github.com/wavefronthq/wavefront-opentracing-sdk-go/reporter"
            wftracer "github.com/wavefronthq/wavefront-opentracing-sdk-go/tracer"
            application "github.com/wavefronthq/wavefront-sdk-go/application"
            wavefront "github.com/wavefronthq/wavefront-sdk-go/senders"
        )

        func NewGlobalTracer(serviceName string) io.Closer {
            config := &wavefront.ProxyConfiguration{
                Host:        "<PROXY_IP/PROXY_FQDN>",
                TracingPort: <PROXY_TRACING_PORT>,
            }
            sender, err := wavefront.NewProxySender(config)
            if err != nil {
                log.Printf("Couldn't create Wavefront Sender: %s\n", err.Error())
                os.Exit(1)
            }

            appTags := application.New("beachshirts", serviceName)

            directrep := wfreporter.New(sender, appTags)
            consolerep := wfreporter.NewConsoleSpanReporter(serviceName)

            reporter := wfreporter.NewCompositeSpanReporter(directrep, consolerep)

            tracer := wftracer.New(reporter)
            opentracing.SetGlobalTracer(tracer)

            return ioutil.NopCloser(nil)
        }
        ```



2. Rebuild the binary:
    ```bash
    go build -o target/beachshirts  cmd/beachshirts/main.go
    ```

3. Restart all the services:
    ```bash
    ./target/beachshirts conf/shopping.conf
    ./target/beachshirts conf/styling.conf
    ./target/beachshirts conf/delivery.conf
    ```

4. Generate some load via loadgen - Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds.

5. Go to **Applications -> Traces** in the Wavefront UI to visualize your traces. You can also go to **Applications -> Inventory** to visualize the RED metrics that are automatically derived from your tracing spans.
