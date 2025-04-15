#!/bin/bash
./gradlew bootRun &
APP_PID=$!
echo "애플리케이션이 시작되었습니다. 10초 후 종료됩니다. PID: $APP_PID"
sleep 10
echo "애플리케이션을 종료합니다..."
kill $APP_PID
echo "종료 완료"
