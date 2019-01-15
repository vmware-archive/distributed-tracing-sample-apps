Execute the below steps to generate docker images for various services:
```
mvn clean install

docker build -f df-shopping -t us.gcr.io/wavefront-gcp-dev/akodali/shopping-sb .
docker build -f df-styling -t us.gcr.io/wavefront-gcp-dev/akodali/styling-sb .
docker build -f df-delivery -t us.gcr.io/wavefront-gcp-dev/akodali/delivery-sb .

docker push  us.gcr.io/wavefront-gcp-dev/akodali/shopping-sb
docker push  us.gcr.io/wavefront-gcp-dev/akodali/styling-sb
docker push  us.gcr.io/wavefront-gcp-dev/akodali/delivery-sb

```
