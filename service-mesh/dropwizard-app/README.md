# Wavefront Hackthon - Dropwizard App using Istio

This is a sample java application using Dropwizard called beachshirts (#[beachops](https://medium.com/@matthewzeier/thoughts-from-an-operations-wrangler-how-we-use-alerts-to-monitor-wavefront-71329c5e57a8)) which makes cool shirts for beach. 


## Running Application locally 

View (#[Dropwizard App](https://github.com/wavefrontHQ/hackathon/tree/master/3D-microservices-observability/dropwizard-app#wavefront-hackthon---dropwizard-app)) instructions.


## Running Application in Istio:

- Build container images for each service using instructions in (#[Docker Files](https://github.com/wavefrontHQ/hackathon/blob/akodali/sm/3D-microservices-observability/service-mesh/dropwizard-app/dockerFiles/README.md))
- Deploy container images in kubernetes and configure istio networking using instructions in (#[Kubernetes deployment files](https://github.com/wavefrontHQ/hackathon/blob/akodali/sm/3D-microservices-observability/service-mesh/dropwizard-app/kubernetesFiles/README.md))
