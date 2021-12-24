#!/bin/sh

echo "Starting application ..."
java -Xmx256m -Djava.security.egd=file:/dev/./urandom -jar /opt/logreposit/ve-direct-reader-service/app.jar
