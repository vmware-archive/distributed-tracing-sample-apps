# Wavefront Hackthon - Springboot Jersey App

This is a sample java application using Springboot with Jersey called beachshirts which makes cool shirts for beach. 

## Running Application locally 

- Run `mvn clean install` from the root directory of the project.

- Now run all the services using below commands from root directory of the project.

  ```bash
  java -jar ./shopping/target/shopping-0.9.0-SNAPSHOT.jar
  java -jar ./styling/target/styling-0.9.0-SNAPSHOT.jar
  java -jar ./delivery/target/delivery-0.9.0-SNAPSHOT.jar
  ```

- Now view the shopping menus using HTTP GET request: `http://localhost:50050/shop/menu`

- Order shirts using HTTP POST request:

- ```bash
  http://localhost:50050/shop/order
  Payload: {"styleName" : "testStyle1","quantity" : 5}
  ```

- Use `./loadgen {interval}` in the root directory to send a request of ordering shirts every  `{interval}` seconds