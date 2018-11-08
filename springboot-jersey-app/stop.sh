#!/bin/bash

svc=$1
echo "ps -ef | grep '/${svc}/target/${svc}-1.0-SNAPSHOT.jar'| grep -v 'grep' | awk '{ print $2 }'"
pid=$(ps -ef | grep '/${svc}/target/${svc}-1.0-SNAPSHOT.jar'| grep -v 'grep' | awk '{ print $2 }')
echo "stopping ${svc} process: ${pid}"

kill ${pid}
