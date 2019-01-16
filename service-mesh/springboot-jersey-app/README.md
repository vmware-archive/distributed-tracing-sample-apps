# Wavefront Hackathon - Springboot Jersey App using Istio

This is a sample java application using Springboot with Jersey called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8))
which makes shirts for the beach.

## Running Application locally

View (#[Springboot Jersey App](https://github.com/wavefrontHQ/hackathon/tree/master/3D-microservices-observability/springboot-jersey-app#wavefront-hackathon---springboot-jersey-app)) instructions.

## Running Application in Istio:

  - Build container images for each service using instructions in (#[Docker Files](https://github.com/wavefrontHQ/hackathon/blob/akodali/sm/3D-microservices-observability/service-mesh/springboot-jersey-app/dockerFiles/README.md))
  - Deploy container images in kubernetes and configure istio networking using instructions in (#[Kubernetes deployment files](https://github.com/wavefrontHQ/hackathon/blob/akodali/sm/3D-microservices-observability/service-mesh/springboot-jersey-app/kubernetesFiles/README.md))
