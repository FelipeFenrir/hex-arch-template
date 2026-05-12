# API Distribution Adapter (Outbound) - OrderQuestionnaire

Integração com serviço de distribuição (canais e jornadas). Implementa `DistributionOutPort` via Feign.

## Propósito

Validar se um `channelDistributionId` e `journeyDistributionId` existem e estão ativos durante a criação de questionários.

## Configuração

Em `application.yml`:

```yaml
orderquestionnaire:
  adapters:
    out:
      api-distribution:
        base-url: http://localhost:8082  # Wiremock por padrão
        timeout-ms: 5000
```

## Client Feign

```java
@FeignClient(name = "distribution-api", url = "${...base-url}")
public interface DistributionApiClient {
    @GetMapping("/channels/{id}")
    Channel getChannel(@PathVariable String id);
    
    @GetMapping("/journeys/{id}")
    Journey getJourney(@PathVariable String id);
}
```

## Testes

- Wiremock stubs em `docker/wiremock/` para testes de integração

