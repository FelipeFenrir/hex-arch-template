# App Observability Dashboard - Guia de Uso

## Acesso Direto

- URL: `http://localhost:3000`
- Search/Browse > "App Observability" ou UID `app-observability`
- Tempo padrão: últimas 1 hora, refresh a cada 10 segundos

---

## Fluxo de Uso Recomendado

### 1. **Verificação Rápida (status 30s)**

Olhe para o **Overview - KPIs & Golden Signals**:
- ✅ Business Targets Up = 2? Apps estão vivas.
- ✅ Error Rate < 1%? SLO OK, nothing burning.
- ⚠️ p95 Latency > 0.5s? Pode haver pressão de carga ou problema downstream.

**Ação**: Se tudo verde, vá para o próximo item. Se algo vermelho, pule para **Investigação Profunda**.

---

### 2. **Monitoramento Contínuo (5-10 min)**

Verifique a seção **Availability & SLO Compliance**:
- Rode de Business App em Business App (filtro) — olhe se uptime e compliance estão estáveis.
- Se Error Budget está caindo rápido (< 50%), é hora de investigar.

Navegue para **Golden Signals - Detailed View**:
- **Traffic**: esperado variação natural? Ou spike fora do padrão?
- **Errors**: linha reta no zero é bom; qualquer "pico" merece atenção.
- **Latency**: compare p95 vs p99 — se p99 muito maior, há outliers (possível GC).

---

### 3. **Investigação Profunda (quando SLO quebra)**

Vá para **Detailed Analysis - Endpoints & Status Breakdown**:

#### 3a. **Endpoints by Status**
- Qual é o endpoint no topo do throughput?
- Seu status mix está ok (2xx/3xx)? Ou vendo muitos 4xx/5xx?
- Se vendo 5xx, clique na linha ou vá para **Error Endpoints (5xx)** abaixo.

#### 3b. **Error Endpoints (5xx)**
- Top 15 endpoints com erros 5xx.
- Qual endpoint está sendo impactado? `GET /api/questions`? `POST /api/questionnaires`?
- Throughput desse endpoint alto? Pode ser volume ou taxa de erro alta.

#### 3c. **Responses by Status Code**
- Série temporal por status range.
- Se vendo aumento em 5xx e queda em 2xx, é um problema crítico.
- Se vendo aumento em 4xx (bad request), pode ser input mal-formado do cliente.

---

### 4. **Diagnóstico de Saturation (quando performance degrada)**

Vá para **Saturation (Resource Utilization)**:

| Métrica | Verde | Amarelo | Vermelho | Ação |
|---------|-------|---------|----------|------|
| Heap JVM | < 70% | 70-85% | > 85% | Aumentar `-Xmx` ou investigar leak/GC |
| CPU | < 60% | 60-80% | > 80% | Scale out (mais replicas) ou otimizar código |
| Live Threads | < 100 | --- | > pool max | Pool saturado, requisições enfileiradas |
| HTTP/Pool | < 60% | 60-80% | > 80% | Aumentar pool ou investigar threads bloqueadas |

**Próximos passos**:
- **Heap > 85%**: Olhe logs do GC (Spring Boot actuator `/actuator/health/diskSpace`). Considere reduzir retenção de dados em cache.
- **CPU > 80%**: Usar dashboard de tracing (`Trace Topology`) para identificar endpoint/serviço lento.
- **Threads vivas crescendo**: Possível thread leak. Investigate aplicação code via logs estruturados.

---

## Filtros: O que Cada Um Faz

### **Business App**
- Padrão: **All** (mostra todos: security-server + orderquestionnaire)
- Selecione **security-server** para focar naquele serviço isolado
- Útil para comparar performance entre apps lado-a-lado

### **Platform Component**
- Padrão: **All** (prometheus, otel-collector, loki, tempo)
- Reduz para monitorar saúde de componentes específicos de observabilidade
- Se loki está offline, coleta de logs para rastreamento abaixa

### **Rate Window**
- **1m**: sensível, mostra picos/vales rápidos. Bom para detecção rápida de anomalias.
- **5m** (padrão): smoothed, melhor visibilidade de trends. Ideal para SLO.
- **15m**: muito smoothed. Use se noise é alto.

### **HTTP Method**
- Padrão: **All** (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)
- Selecione **POST** para focar apenas em requisições que modificam dados
- Selecione **GET** para focar em leituras
- Útil para isolar problemas: "Erros acontecem apenas em POST /questionnaires?" → selecione POST para confirmar

### **Quick Jump Buttons** (novo)
- Painel "Quick Navigation: Jump to Trace Topology" oferece 4 atalhos:
  - 📊 **View ALL Error Traces**: salta para Trace Topology mostrando TODOS os erros
  - 🔍 **View Traces for Selected App**: filtra por business app selecionado
  - 📋 **View Logs in Loki**: salta para Logs Loki com mesmo filtro de app
  - 🕐 **View Request Timeline**: abre Trace Investigation dashboard

---

## Exemplos de Interrogações Rápidas

### "Por que a latência subiu?"
1. Filtro: selecione Business App em questão
2. Olhe **Errors**: se erro também subiu, problema é na aplicação; se erro flat, problema é nas dependências
3. Olhe **Saturation**: Heap ou CPU está alto? Sim = gargalo local; Não = problema downstream
4. Vá para **Error Endpoints**: qual endpoint está lento?
5. Copie o trace ID do log e investigue no dashboard **Trace Topology**

### "Como está meu erro budget?"
1. Seção **Availability & SLO Compliance**
2. Card **Error Budget (remaining)**
3. Se < 25%, PARE — investigue erros antes de deploy

### "Qual app está em pior estado?"
1. Seção **Availability & SLO Compliance**
2. Table **SLO Status by App** — mostra erro rate de cada uma
3. Filtre Business App para a app em vermelho
4. Drill-down em **Error Endpoints** para diagnosticar

### "A gente está escalando bem?"
1. Filtro: deixe **Business App** = All
2. **Traffic** — linha subindo steadily? Bom, volume crescendo
3. **Latency** — p95 subiu também? Não escalou o suficiente
4. **Saturation - Heap/CPU**: estão subindo juntos com traffic? Estão em good shape

---

## Integração com Outros Dashboards

### Navegação Automática para Traces (novo)

O dashboard agora oferece **5 "portais"** para pular direto para o Trace Topology:

1. **Tabela "Error Endpoints (5xx) - with Trace Link"**
   - Cada linha de erro tem um menu de contexto (right-click)
   - Opção 1: 🔍 "Trace this error in Trace Topology"
   - Opção 2: 📊 "View logs in Loki (5xx errors)"
   - Leva automaticamente com filtros pré-configurados (app + method + status)

2. **Série Temporal "Responses by Status Code"**
   - Série `5xx` é destacada em **vermelho bold**
   - Clique em qualquer ponto da série 5xx
   - Abre Trace Topology com level=ERROR
   
3. **série "Errors (5xx rate %) by App"**
   - Hover sobre a série de erro de uma app
   - Clique no símbolo de link que aparece
   - Salta para Trace Topology com `var-app=` selecionada

4. **Tabela "SLO Status by App"**
   - Clique em qualquer app name (quando error rate está vermelho)
   - Abre Trace Topology com contexto daquela app

5. **Botões "Quick Navigation: Jump to Trace Topology"** (novo painel)
   - Localizado logo acima da seção "Platform Infrastructure Health"
   - 4 botões configuráveis:
     - Todos os erros globais
     - Erros da app selecionada
     - Logs estruturados da Loki
     - Timeline de requisições (Trace Investigation)

### Fluxo Recomendado: Detecção → Rastreamento

```
Passo 1: App Observability (este dashboard)
  ↓
  Vejo erro_rate alto? → Tabela "Error Endpoints (5xx)"
  ↓
Passo 2: Clico na linha do erro → Link abre Trace Topology
  ↓
Passo 3: Trace Topology mostra o call chain
  ↓
  Encontro o serviço lento/falhando?
  ↓
Passo 4: Clico "View Logs" → Loki mostra logs estruturados daquele span
  ↓
Passo 5: Encontro o stack trace → Corrijo no código
```

### Rastreamento até o código
1. Veja erro na seção **Error Endpoints (5xx)**
2. Copie o **method** e **uri** da tabela
3. Clique no link 🔍 (automático) → Trace Topology abre
4. Procure o span lento/falhado
5. Clique "View Logs" (link dentro do Trace Topology) → Logs Loki
6. Veja logs estruturados com stack trace completo

### Análise de logs estruturados
1. Em **Error Endpoints**, identifique o endpoint problemático
2. Vá para dashboard **Logs JSON Loki**
3. Filtre por:
   - App: a mesma app
   - Correlation ID: se tiver na trace
   - Endpoint/URI: busque a chamada HTTP
4. Veja logs estruturados com stack trace completo

### Comparação com resource constraints
1. Se Saturation > 80%, vá para **Platform Infrastructure Health**
2. Verifique: Prometheus/Loki/Tempo estão coletando dados?
3. Se **Platform Scrape Duration** está alto, coleta está lenta — pode estar machine overloaded

---

## Exemplo de Cenário: Alerts em Produção

**Cenário**: Notificação: "Sistema reportou +50% de erros 5xx nos últimos 2 min"

**Resposta investigativa com o dashboard**:

```
Step 1: Overview - KPIs
→ Error Rate: 2.5% (estava 0.5%, spike 5x)
→ p95 Latency: 0.8s (era 0.2s, degradação de 4x)
→ Business Targets Up: 2/2 OK (apps vivas)

Step 2: Availability & SLO Compliance
→ SLO Compliance: RED (FAIL) — ultrapassou threshold 1%
→ Error Budget: 0% — Consumido!

Step 3: Error Endpoints (5xx)
→ Top endpoint: POST /api/questionnaires (500 err/min)
→ 2º endpoint: GET /api/questions (100 err/min)

Step 4: Saturation
→ Heap: 88% (VERMELHO) — possível GC pause
→ CPU: 75% (AMARELO)
→ Live Threads: 180/200 (próximo ao max)

Step 5: Ação Recomendada
→ Rápido: Aumentar heap heap inicial no bootstrap
→ Médio: Investigar POST /api/questionnaires via Trace Topology
→ Longo: Otimizar cálculos heavy ou implementar cache
```

---

## Dicas d'Ouro

1. **Sempre começar do Overview**: Em 10 segundos você sabe se está tudo bem.
2. **Error Budget é seu melhor amigo**: Gerencia quando fazer deploy sem quebrar SLO.
3. **p99 > p95 5x? Você tem outliers**: GC pauses, slow queries, ou rede intermitente.
4. **Heap sempre crescendo?** Memory leak — investigue com profiler local.
5. **Tráfego plano mas erros subindo?** Problema de qualidade, não de volume — investigar lógica.
6. **Saturation amarela?** Comece a planejar scale-out antes de ficar vermelha.
7. **Integre com tracing**: Drill-down em endpoint no App Observability → vá para Trace Topology para ver call chain completo.

---

## Troubleshooting do Dashboard

### "Vejo NaN em alguns painéis"
**Causa**: Métrica não está sendo coletada da aplicação.
**Fix**: Verifique:
- Aplicação está rodando e enviando metrics (porta `/actuator/prometheus`)?
- Target no `prometheus.yml` está correto?
- Filtro Business App está muito restritivo?

### "Todos os status estão "0" (vermelho constante)"
**Causa**: Nenhuma métrica chegando ao Prometheus.
**Fix**:
- Verificar se OTEL está exportando para Prometheus
- Verificar logs do Prometheus:`docker logs prometheus`
- Verificar se target está down: ir para Prometheus UI (`http://localhost:9090`) > Targets

### "Drill-down tables ficam em branco"
**Causa**: Métrica `http_server_requests_seconds` não está sendo emitida.
**Fix**:
- Confirmar micrometer/actuator rodando na app com `curl http://localhost:9005/actuator/prometheus | grep http_server`
- Se nenhum hit, aumentar carga (gerar requisições) antes de olhar o dashboard

---

## Próximos Passos Após Diagnóstico

1. **Para Performance**: use **Trace Topology** para drill-down em latência
2. **Para Confiabilidade**: use **Logs JSON Loki** para correlacionar logs via traceId
3. **Para Planejamento**: use **Error Budget** para decidir quando deploy
4. **Para Long-term**: implemente alertas (Prometheus AlertManager) baseado em thresholds deste dashboard

