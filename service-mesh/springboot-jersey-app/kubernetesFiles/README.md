Execute the below commands to deploy the service in kubernetes and configure networking required for Istio ingress to connect to the landing service.

```
kubectl apply -f <(istioctl kube-inject -f beachshirts.yml)

cd networking-istio/

kubectl apply -f gateway.yml
```


Verify your services are up and running using:
```
kubectl get pods
kubectl get svc
```
