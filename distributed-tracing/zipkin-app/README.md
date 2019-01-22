# Wavefront Hackathon - Zipkin App

This is a sample java application using Dropwizard called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for beach. 


## Running the Application locally 

1. Run Zipkin in your env using the [Docker image](https://zipkin.io/pages/quickstart.html):

   ```bash
   $ docker run -d -p 9411:9411 openzipkin/zipkin
   ```

2. `git clone` this repo and navigate to this dir:

   ```bash
   git clone https://github.com/wavefrontHQ/hackathon.git
   cd hackathon/distributed-tracing/zipkin-app
   ```

4. Run `mvn clean install` from the root directory of this project.

5. Now run all the services using the commands below:

   ```bash
   java -jar ./shopping/target/shopping-1.0-SNAPSHOT.jar server ./shopping/app.yaml
   java -jar ./styling/target/styling-1.0-SNAPSHOT.jar server ./styling/app.yaml
   java -jar ./delivery/target/delivery-1.0-SNAPSHOT.jar server ./delivery/app.yaml
   ```

- Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.
- Now go to Zipkin UI (http://localhost:9411, if you're using the zipkin docker image from above) and look for the traces for service "shopping" and click on Find Traces.

6. Stop loadgen.

## Switching from Zipkin to Wavefront
1. Kill the zipkin server. 

   ```bash
   // Kill the zipkin container.
   $ docker ps -a | awk '{ print $1,$2 }' | grep openzipkin/zipkin | awk '{print $1 }' | xargs -I {} docker stop {}
   // Verify no zipkin containers are running. The below command should not return any containers.
   $ docker container ls | grep openzipkin/zipkin 
   ```
   
2. Configure the [Wavefront Zipkin Integration](https://docs.wavefront.com/zipkin.html#zipkin-integration-setup) with proxy version >= 4.35
   * Set `traceZipkinListenerPorts` to use port (9411) as this is the port the beachshirts application sends traces to.

3. Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds. You will see some random failures which are added by us.

11. Now all the traces should be sent to Wavefront via the Wavefront Proxy. Go to Browse -> Traces in the Wavefront UI to view your traces. Application name is defaulted to `zipkin`.
