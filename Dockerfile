FROM hmcts/cnp-java-base:openjdk-8u181-jre-alpine3.8-1.0

ENV APP div-case-orchestration-service.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 56

COPY build/libs/$APP /opt/app/

WORKDIR /opt/app

EXPOSE 4012
