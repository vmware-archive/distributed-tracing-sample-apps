#!/bin/bash

echo "Send requests every $1 seconds"

while [ true ]
do
	quantity=$((1 + RANDOM % 20))
	curl_cmd="curl -X POST http://localhost:50050/api/shop/order -H 'Content-Type: application/json' -d '{\"styleName\": \"BeachOps\", \"quantity\": '$quantity'}'"
	echo $curl_cmd
	eval $curl_cmd
	sleep $1
	printf "\n"
done
