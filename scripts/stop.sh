#!/bin/bash

APP_NAME="airbob-0.0.1-SNAPSHOT.jar"
PID=$(pgrep -f $APP_NAME)

if [ -n "$PID" ]; then
  kill -15 $PID
  echo "Stopped process: $PID"
else
  echo "No process found to stop"
fi
