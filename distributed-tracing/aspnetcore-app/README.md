# sample-app-csharp

#### This Beach Shirts app contains 3 ASP.NET Core Web API services:
- BeachShirts.Shopping
- BeachShirts.Styling
- BeachShirts.Delivery

## Requirement
`docker` and `docker-compose` installed.

## Build
```bash
docker-compose build
```

## Run
```bash
$ docker-compose up

$ curl -X GET http://localhost:50050/api/shop/menu
# Result: [{"name":"Wavefront","imageUrl":"WavefrontUrl"},
#          {"name":"BeachOps","imageUrl":"BeachOpsUrl"}]

$ curl -X POST http://localhost:50050/api/shop/order \
  -H 'Content-Type: application/json' \
  -d '{"styleName": "Wavefront", "quantity": 5}'
# Result: {"orderNum":"abc123...","trackingNum":"def456...","status":"shirts delivery dispatched"}
```