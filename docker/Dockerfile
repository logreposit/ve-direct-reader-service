FROM eclipse-temurin:21-jre

MAINTAINER Dominic Miglar <dominic.miglar@netunix.at>

WORKDIR /opt/logreposit/ve-direct-reader-service

COPY run.sh    /opt/logreposit/ve-direct-reader-service/run.sh
COPY app.jar   /opt/logreposit/ve-direct-reader-service/app.jar
RUN  chmod 755 /opt/logreposit/ve-direct-reader-service/run.sh

ENTRYPOINT ["/opt/logreposit/ve-direct-reader-service/run.sh"]
