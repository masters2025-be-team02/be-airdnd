#!/bin/bash

APP_NAME="airbob-0.0.1-SNAPSHOT.jar"
APP_DIR="/home/ubuntu/app"
LOG_FILE="$APP_DIR/app.log"
JAVA_CMD=$(which java)

echo "DB URL: $SPRING_DATASOURCE_URL" >> "$LOG_FILE"
cd "$APP_DIR"
export PATH=$PATH:/usr/bin

while ! nc -z airbob.ct8esm2iib95.ap-northeast-2.rds.amazonaws.com 3306; do
  echo "Waiting for RDS..." >> "$LOG_FILE"
  sleep 2
done

echo "Starting $APP_NAME at $(date)" >> "$LOG_FILE"
nohup "$JAVA_CMD" -jar "$APP_NAME" >> "$LOG_FILE" 2>&1 &
