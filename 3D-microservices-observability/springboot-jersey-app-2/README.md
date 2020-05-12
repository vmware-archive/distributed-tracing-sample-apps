# Wavefront Hackathon - Springboot Jersey App 2

This is a sample java application using Springboot beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) 
which makes shirts for the beach. The application is based on spring boot web using rest templates and rest annotations.
- JDK 1.8 and up
- Spring Boot 2.3.0.RC1

## Running Application locally 

- `git clone` this repo and navigate to this dir:

- ```bash
  git clone https://github.com/howardyoo/distributed-tracing-sample-apps/
  cd ./distributed-tracing-sample-apps/3D-microservices-observability/springboot-jersey-app-2
  ```
- Run `mvn clean compile` from the root directory of the project.

- Now run all the services using below commands from root directory of the project.

  ```bash
  mvn -pl registration spring-boot:run &
  mvn -pl delivery spring-boot:run &
  mvn -pl styling spring-boot:run &
  mvn -pl shopping spring-boot:run &
  ```
- If you are using an IDE like intellij, you can also group these mvn projects together to start them up in a group.

- Now view the shopping menus using HTTP GET request: `curl -X GET http://localhost:4444/menu`

- Order shirts using HTTP POST request:

- ```bash
  curl -X POST -d '{"styleName":"style1", "quantity":10}' -H "Content-Type: application/json" http://localhost:4444/order
  ```

- Use `./loadgen.sh {interval}` in the root directory to send a request of ordering shirts every `{interval}` seconds

- This application uses spring Eureka for service discovery. `registration` is the Eureka server running on port 1111. 
  Use your browser to open `http://localhost:1111` to monitor the three services `delivery`, `styling`, and `shopping`.
  
## Enabling Wavefront Spring Boot Starter ##
- Uncomment the following entries in `pom.xml` in order to use Wavefront freemium to track your application performance.
- Uncommenting `wavefront-spring-boot-starter` is mandatory to auto configure metrics, but you need to choose either
  SLEUTH or Opentracing depending on your preference. For example, uncomming `<-- for SLEUTH -->` part to enable 
  `spring-cloud-starter-sleuth` in order to have SLEUTH produce distributed tracing for the application, or you can
  use opentracing instead.
```xml
	<dependencies>
...
        <!-- for SLEUTH
        	<dependency>
        		<groupId>org.springframework.cloud</groupId>
        		<artifactId>spring-cloud-starter-sleuth</artifactId>
        		<version>2.2.2.RELEASE</version>
        	</dependency>
        -->
        
        <!-- for Observability using Wavefront
        	<dependency>
        		<groupId>com.wavefront</groupId>
        		<artifactId>wavefront-spring-boot-starter</artifactId>
        		<version>2.0.0-RC1</version>
        	</dependency>
        -->
        
        <!-- for Opentracing
        	<dependency>
          		<groupId>io.opentracing.contrib</groupId>
          		<artifactId>opentracing-spring-cloud-starter</artifactId>
        		<version>LATEST</version>
        	</dependency>
        -->
...
   </dependencies>
```
- Follow the instructions in the standard output regarding the access to Wavefront freemium. Once you navigate to your Wavefront UI,
  you should be able to see your metrics and traces shown. You may need to wait about a minute for the first flow of metrics to be shown
  in the UI, and also make sure to run loadgen.sh with seconds interval e.g. `loadgen.sh 5` in order to produce some test loads.
