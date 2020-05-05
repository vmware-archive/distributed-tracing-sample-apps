# Wavefront Hackathon - Springboot Jersey App 2

This is a sample java application using Springboot beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) 
which makes shirts for the beach. 

## Running Application locally 

- `git clone` this repo and navigate to this dir:

- ```bash
  git clone https://github.com/wavefrontHQ/hackathon.git
  cd hackathon/3D-microservices-observability/springboot-jersey-app-2
  ```
- Run `mvn clean install` from the root directory of the project.

- Now run all the services using below commands from root directory of the project.

  ```bash
  mvn -pl registration spring-boot:run
  mvn -pl delivery spring-boot:run
  mvn -pl styling spring-boot:run
  mvn -pl shopping spring-boot:run
  ```

- Now view the shopping menus using HTTP GET request: `curl -X GET http://localhost:4444/menu`

- Order shirts using HTTP POST request:

- ```bash
  curl -X POST -d '{"styleName":"style1", "quantity":10}' -H "Content-Type: application/json" http://localhost:4444/order
  ```

- Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds

