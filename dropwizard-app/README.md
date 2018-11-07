# Wavefront Hackthon - Dropwizard App

This is a sample java application using Dropwizard called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for beach. 


## Running Application locally 

- Run `mvn clean install` from the root directory of the project.

- Now run all the services using below commands from root directory of the project.

  ```bash
  java -jar ./shopping/target/shopping-1.0-SNAPSHOT.jar server ./shopping/app.yaml
  java -jar ./styling/target/styling-1.0-SNAPSHOT.jar server ./styling/app.yaml
  java -jar ./delivery/target/delivery-1.0-SNAPSHOT.jar server ./delivery/app.yaml
  ```

- Now view the shopping menus using HTTP GET request: `http://localhost:50050/shop/menu`

- Order shirts using HTTP POST request:

- ```bash
  http://localhost:50050/shop/order
  Payload: {"styleName" : "testStyle1","quantity" : 5}
  ```

- Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds
