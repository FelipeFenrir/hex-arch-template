# Bootstrap Module - OrderQuestionnaire

Montador da aplicação Spring Boot. Agrega todos os módulos (domain, application, adapters) e expõe a aplicação para execução.

## Estrutura

```
bootstrap/
├── src/
│   ├── main/
│   │   ├── java/com/acme/orderquestionnaire/
│   │   │   ├── OrderQuestionnaireApplication.java    # @SpringBootApplication
│   │   │   └── config/
│   │   │       ├── pipeline/                         # Configs de case de uso
│   │   │       │   ├── CreateQuestionnaireUseCaseConfig.java
│   │   │       │   ├── ValidateQuestionnaireAnswersUseCaseConfig.java
│   │   │       │   └── ...
│   │   │       ├── adapter/
│   │   │       │   ├── in/                           # Configs de adapters entrada
│   │   │       │   └── out/                          # Configs de adapters saída
│   │   │       └── DataSourceConfig.java
│   │   └── resources/
│   │       ├── application.yml                       # Config principal
│   │       ├── logback-spring.xml                    # Logging
│   │       └── templates/                            # Thymeleaf (scaffold)
│   └── test/
│       └── ...
└── build.gradle
```

## Application.yml

Propriedades centrais:

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  application:
    name: orderquestionnaire
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27017/orderquestionnaire}

question:
  pipeline:
    create:
      enabled: true
      order: [...]

questionnaire:
  pipeline:
    create:
      enabled: true
      order: [...]
    validate-answers:
      enabled: true
      order: [command_step, fetch_step, validate_step]

orderquestionnaire:
  adapters:
    in:
      api-rest:
        cors: {...}
    out:
      api-distribution:
        base-url: ${DISTRIBUTION_API_URL:http://localhost:8082}
```

## OrderQuestionnaireApplication

```java
@SpringBootApplication(scanBasePackages = "com.acme.orderquestionnaire")
@EnableMongoRepositories(basePackages = "com.acme.orderquestionnaire")
public class OrderQuestionnaireApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderQuestionnaireApplication.class, args);
    }
}
```

Execução local:

```bash
./gradlew :orderquestionnaire:modules:bootstrap:bootRun
```

## Build e Entrega

### Docker

```dockerfile
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY build/libs/bootstrap-0.1.0-SNAPSHOT.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "$JAVA_OPTS", "-jar", "app.jar"]
```

Build:

```bash
./gradlew :orderquestionnaire:modules:bootstrap:bootJar
docker build -f orderquestionnaire/modules/bootstrap/Dockerfile -t acme/orderquestionnaire:local orderquestionnaire/modules/bootstrap
```

## Observabilidade

OTEL via `application.yml`:

```yaml
otel:
  exporter:
    otlp:
      endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}
  resource:
    attributes: service.name=orderquestionnaire,service.namespace=acme
```

Exposição:

- `/actuator/health`: K8s liveness/readiness
- `/actuator/prometheus`: Prometheus metrics
- Traces: OTEL Collector → Tempo
- Logs: JSON via Logback → Loki

## Testes

- **Unit**: via cada módulo (`./gradlew :orderquestionnaire:modules:*:test`)
- **Integration**: Testcontainers + Real DB/APIs em `bootstrap/src/test/`
- **Architecture**: ArchUnit em `modules/application` e `modules/domain`

