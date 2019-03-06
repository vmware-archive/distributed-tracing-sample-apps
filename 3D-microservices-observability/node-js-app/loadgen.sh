#!/bin/bash

while true; do
    read -p "Please input API call interval in sec " input
    if [[ $input =~ ^[0-9]+$ ]] ; then
        echo "Send requests every $input seconds"

        while [ true ]
        do
            quantity=$((1 + RANDOM % 20))
            curl_cmd="curl -X POST http://localhost:3000/shop/order -H 'Content-Type: application/json' -d '{\"styleName\": \"beachops\", \"quantity\": '$quantity'}'"
            echo $curl_cmd
            eval $curl_cmd
            sleep $input
            printf "\n"
        done
    else
        echo "Please input an integer "
    fi
done