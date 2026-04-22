# modules/bootstrap/Dockerfile
FROM eclipse-temurin:25-jre as runtime
WORKDIR /app
ARG JAR=bootstrap-0.1.0-SNAPSHOT.jar
COPY build/libs/${JAR} app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENV OTEL_EXPORTER_OTLP_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT:-http://otel-collector:4317}
EXPOSE 8080
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
# Logs vão para stdout/stderr para coleta pelo Promtail