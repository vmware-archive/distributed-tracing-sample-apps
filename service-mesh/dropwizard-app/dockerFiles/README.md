Execute the below steps to generate docker images for various services:
```
mvn clean install

docker build -f df-shopping -t us.gcr.io/wavefront-gcp-dev/akodali/shopping .
docker build -f df-styling -t us.gcr.io/wavefront-gcp-dev/akodali/styling .
docker build -f df-delivery -t us.gcr.io/wavefront-gcp-dev/akodali/delivery .

docker push  us.gcr.io/wavefront-gcp-dev/akodali/shopping
docker push  us.gcr.io/wavefront-gcp-dev/akodali/styling
docker push  us.gcr.io/wavefront-gcp-dev/akodali/delivery

```
