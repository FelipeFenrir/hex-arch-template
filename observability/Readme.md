# Observability Library

Biblioteca compartilhada para observabilidade em projetos Spring Boot do ecossistema `acme`.

Ela centraliza:
- instrumentacao por anotacao (`@Loggable`)
- logging estruturado no `LoggingAspect`
- mascaramento de dados sensiveis (`LogSanitizer`)
- configuracao externa via propriedades (`ObservabilityLoggingProperties`)

## Objetivo

Evitar que cada servico implemente seu proprio aspecto de logs do zero.
Com essa lib, os servicos reaproveitam uma base comum e configuravel.

## Como consumir em outro projeto

1. Adicione a dependencia da biblioteca no modulo que sobe o contexto Spring:

```groovy
implementation 'com.acme:observability'
```

2. Marque classes/metodos com `@Loggable`.

3. Configure as propriedades no `application.yml` (opcional, existem defaults).

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

## Como a auto-configuracao entra no Spring Boot

No Spring Boot 3, bibliotecas podem registrar auto-configuracoes por meio do arquivo:

`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

Neste projeto, o arquivo contem:

```text
com.acme.observability.config.ObservabilityAutoConfiguration
```

Isso diz ao Boot: "quando essa biblioteca estiver no classpath, importe essa auto-configuracao".

### Por que isso e importante?

Sem esse arquivo, o projeto consumidor teria que fazer `@Import(...)` manualmente para registrar os beans.
Com ele, a biblioteca funciona como um "mini starter": plugou a dependencia, a configuracao entra automaticamente (respeitando os `@Conditional...`).

## Beans criados automaticamente

A classe `ObservabilityAutoConfiguration` registra:
- `LogSanitizer`
- `LoggingAspect` (condicional a `acme.observability.logging.enabled=true`)

Assim, o comportamento de logging pode ser ligado/desligado por propriedade, sem alterar codigo.

## Propriedades suportadas

Definidas em `ObservabilityLoggingProperties` (prefixo `acme.observability.logging`):

- `enabled` (default: `true`): habilita/desabilita o aspecto.
- `log-arguments` (default: `true`): registra argumentos de entrada.
- `log-result` (default: `true`): registra retorno do metodo.
- `max-payload-length` (default: `4000`): trunca payloads grandes para reduzir risco de vazamento e volume.
- `sensitive-fields`: lista de chaves que devem ser mascaradas.

## Mascaramento de dados sensiveis

`LogSanitizer` aplica mascara recursiva em objetos, colecoes e mapas.
Campos sensiveis sao substituidos por `***` antes da serializacao de log.

Exemplo de chaves comuns:
- `password`
- `token`
- `authorization`
- `clientSecret`
- `cpf`
- `email`

## Fluxo rapido de funcionamento

1. O metodo anotado com `@Loggable` e interceptado por `LoggingAspect`.
2. O aspecto monta payload estruturado (`method.start`, `method.success`, `method.error`).
3. `LogSanitizer` mascara/trunca dados conforme propriedades.
4. O appender (`logback-spring.xml`) escreve JSON para console/arquivo.

## Dicas para iniciantes

- Se nao aparecer log do aspecto, verifique se:
  - a dependencia `com.acme:observability` esta no modulo que sobe o Spring
  - a classe/metodo possui `@Loggable`
  - `acme.observability.logging.enabled` nao esta `false`
- Evite logar payload completo em fluxos de autenticacao e dados pessoais.
- Mantenha `correlationId` e `flowId` no formato final de log para facilitar rastreabilidade.

## Exemplo de `logback-spring.xml`

Para padronizar logs JSON com `correlationId` e `flowId`, um projeto consumidor pode usar:

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
