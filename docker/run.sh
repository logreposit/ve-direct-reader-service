#!/bin/sh

echo "Starting application ..."
java -Djava.security.egd=file:/dev/./urandom -jar /opt/logreposit/ve-direct-reader-service/app.jar
