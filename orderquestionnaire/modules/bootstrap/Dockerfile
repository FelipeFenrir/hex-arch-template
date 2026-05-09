# modules/bootstrap/Dockerfile
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app
ARG JAR=bootstrap-0.1.0-SNAPSHOT.jar
COPY build/libs/${JAR} app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
ENV ORDERQUESTIONNAIRE_SERVER_PORT=9005
EXPOSE ${ORDERQUESTIONNAIRE_SERVER_PORT}
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
# Logs vao para stdout/stderr para coleta pelo Promtail
