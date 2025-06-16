ARG APP_INSIGHTS_AGENT_VERSION=3.7.0
ARG PLATFORM=""
FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless

ENV APP div-case-orchestration-service.jar
COPY lib/applicationinsights.json /opt/app/

COPY build/libs/$APP /opt/app/

EXPOSE 4012

CMD ["div-case-orchestration-service.jar"]
