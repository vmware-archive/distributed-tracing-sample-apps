#!/bin/sh

dt=$(date '+%d%m%Y%H%M%S');
svc=$1

echo "starting ${svc} service"
java -jar ./${svc}/target/${svc}-1.0-SNAPSHOT.jar > ${svc}_${dt}.out 2>&1 &
