#!/bin/sh
k6 run /scripts/loadtest.js --out experimental-prometheus-rw=http://prometheus:9090/api/v1/write
#k6 run /scripts/loadtest.js --out influxdb=http://influxdb:8086/k6
