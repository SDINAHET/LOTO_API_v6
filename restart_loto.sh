#!/bin/bash

PID=$(lsof -ti:8082)

if [ ! -z "$PID" ]; then
    echo "Killing process $PID"
    kill $PID
    sleep 3
fi

echo "Starting Loto API..."
nohup java -jar /root/Loto_API_prod/Loto_API_prod.jar > app.log 2>&1 &
