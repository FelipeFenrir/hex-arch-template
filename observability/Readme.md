# Observability Library

Biblioteca compartilhada para observabilidade em projetos Spring Boot do ecossistema `acme`.

Ela centraliza:
- instrumentacao por anotacao (`@Loggable`)
- logging estruturado no `LoggingAspect`
- mascaramento de dados sensiveis (`LogSanitizer`)
- configuracao externa via propriedades (`ObservabilityLoggingProperties`)

## Estrutura do modulo

Pacotes principais em `src/main/java/com/acme/observability`:

- `Loggable`: anotacao para marcar pontos de log
- `LoggingAspect`: aspecto que registra `method.start`, `method.success`, `method.error`
- `LogSanitizer`: mascara campos sensiveis e trunca payloads grandes
- `config/ObservabilityAutoConfiguration`: auto-configuracao da biblioteca
- `config/ObservabilityLoggingProperties`: propriedades do prefixo `acme.observability.logging`

## Como consumir em outro projeto

1. Adicione a dependencia no modulo que sobe o contexto Spring:

```groovy
implementation 'com.acme:observability'
```

2. Marque classes/metodos com `@Loggable`.
3. Configure propriedades no `application.yml` (opcional; existem defaults).

Exemplo:

```yaml
acme:
  observability:
    logging:
      enabled: true
      log-arguments: true
      log-result: true
      max-payload-length: 4000
      sensitive-fields:
        - password
        - token
        - authorization
        - cpf
```

## Auto-configuracao no Spring Boot

No Spring Boot 3, a auto-configuracao e registrada em:

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

Conteudo atual:

```text
com.acme.observability.config.ObservabilityAutoConfiguration
```

Com isso, ao adicionar a dependencia, os beans entram automaticamente no contexto (respeitando condicionais).

## Beans criados automaticamente

`ObservabilityAutoConfiguration` registra:

- `LogSanitizer`
- `LoggingAspect` (condicional a `acme.observability.logging.enabled=true`)

## Propriedades suportadas

Definidas em `ObservabilityLoggingProperties` (prefixo `acme.observability.logging`):

- `enabled` (default: `true`)
- `log-arguments` (default: `true`)
- `log-result` (default: `true`)
- `max-payload-length` (default: `4000`)
- `sensitive-fields`

## Fluxo rapido

1. Metodo anotado com `@Loggable` e interceptado por `LoggingAspect`.
2. O aspecto monta payload estruturado de inicio/sucesso/erro.
3. `LogSanitizer` mascara/trunca dados conforme propriedades.
4. O appender (`logback-spring.xml`) escreve JSON para console/arquivo.

## Build e testes

Use o wrapper Gradle da raiz do repositorio (`hex-arch-template/`):

```powershell
.\gradlew.bat :observability:clean :observability:build
.\gradlew.bat :observability:test
.\gradlew.bat :observability:jacocoTestReport
```

## Dicas para iniciantes

- Se nao aparecer log do aspecto, confirme:
  - dependencia `com.acme:observability` no modulo que sobe Spring
  - anotacao `@Loggable` na classe/metodo
  - `acme.observability.logging.enabled` diferente de `false`
- Em fluxos sensiveis, evite logar payload bruto completo.
- Mantenha `correlationId` e `flowId` no formato final de log.

## Exemplo de `logback-spring.xml`


```xml
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    <springProperty scope="context" name="APP_NAME" source="spring.application.name" defaultValue="my-service"/>
    <property name="LOG_PATH_FILE" value="${LOG_FILE:-build/logs/app.log}"/>

    <appender name="CONSOLE_JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp>
                    <fieldName>timestamp</fieldName>
                    <timeZone>UTC</timeZone>
                </timestamp>
                <pattern>
                    <pattern>{"service":"${APP_NAME}","level":"%level","logger":"%logger{36}","thread":"%thread","correlationId":"%mdc{correlationId:-unknown}","flowId":"%mdc{flowId:-unknown}"}</pattern>
                </pattern>
                <message/>
                <mdc>
                    <excludeMdcKeyName>correlationId</excludeMdcKeyName>
                    <excludeMdcKeyName>flowId</excludeMdcKeyName>
                </mdc>
                <stackTrace>
                    <fieldName>stackTrace</fieldName>
                </stackTrace>
            </providers>
        </encoder>
    </appender>

    <appender name="FILE_JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH_FILE}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH_FILE}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>7</maxHistory>
            <totalSizeCap>200MB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp>
                    <fieldName>timestamp</fieldName>
                    <timeZone>UTC</timeZone>
                </timestamp>
                <pattern>
                    <pattern>{"service":"${APP_NAME}","level":"%level","logger":"%logger{36}","thread":"%thread","correlationId":"%mdc{correlationId:-unknown}","flowId":"%mdc{flowId:-unknown}"}</pattern>
                </pattern>
                <message/>
                <mdc>
                    <excludeMdcKeyName>correlationId</excludeMdcKeyName>
                    <excludeMdcKeyName>flowId</excludeMdcKeyName>
                </mdc>
                <stackTrace>
                    <fieldName>stackTrace</fieldName>
                </stackTrace>
            </providers>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE_JSON"/>
        <appender-ref ref="FILE_JSON"/>
    </root>
</configuration>
```

Observacao: `correlationId` e `flowId` continuam presentes no log (campos de topo via `<pattern>`). O bloco `<mdc>` com `excludeMdcKeyName` evita apenas duplicacao dessas duas chaves dentro do objeto `mdc`.
