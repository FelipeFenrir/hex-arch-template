# App Observability Dashboard - Quick Reference

## 1️⃣ Verificação de Saúde (30 segundos)

| O que ver | Local | Esperado | Ação se ruim |
|-----------|-------|----------|-------------|
| Apps vivas? | **Overview** - Business Targets Up | 2/2 | Investigar `docker logs`, Prometheus targets |
| Throughput OK? | **Overview** - Total Throughput | 10-1000 req/s (ou baseline) | Possível DDoS ou queda de usuários |
| Erros OK? | **Overview** - Error Rate | < SLO threshold (1% default) | Saltar para **Error Endpoints (5xx)** |
| Latência OK? | **Overview** - p95 Latency | < 0.5s (ou SLA) | Saturação ou downstream lento |

---

## 2️⃣ Troubleshooting Rápido

### "Erro Rate subiu"
```
1. Filtro Business App → selecione a app com erro
2. Vá para "Error Endpoints (5xx)" → qual endpoint?
3. Se POST /api/questionnaires: vê Saturation alto?
   - SIM: heap/cpu/threads → scale ou otimize
   - NÃO: problema aplicacao (bug) → olhe logs, trace
```

### "Latência subiu"
```
1. Olhe "Saturation" → heap/cpu altos?
   - SIM: scale out ou investigar leak
   - NÃO: dependência lenta
2. Vá para "Trace Topology" → veja call chain
3. Procure o serviço que está lento
```

### "Error Budget está 0%"
```
1. STOP TUDO — não faça deploy agora
2. Vá para "Error Endpoints (5xx)" → qual endpoint mata o SLO
3. Corrija o bug/dependência
4. Monitore "Error Rate" até voltar < threshold
5. Error Budget sobe novamente → safe to deploy
```

### "Heap/CPU em vermelho (saturação)"
```
1. Heap > 85%:  aumentar -Xmx; procurar memory leak
2. CPU > 80%:   scale out (replica); otimize bottleneck
3. Threads max:  aumentar pool; investigar thread leak
```

---

## 3️⃣ Filtros Essenciais

| Filtro | Padrão | Use When | Impacto |
|--------|--------|----------|--------|
| **Business App** | All | Focar numa app específica | Todos os painéis de app |
| **Platform Component** | All | Verificar saúde de Prom/Loki | Só painéis "Platform Health" |
| **Rate Window** | 5m | 1m=sensitive, 15m=smoothed | Traffic, Latency, Error queries |
| **SLO Error Threshold** | 1% | 0.1%=strict, 5%=loose | SLO Compliance card color |
| **HTTP Method** | All | POST only failing? GET ok? | Drill-down tables (endpoints) |

---

## 4️⃣ Painéis & O Que Significam

### **KPIs (Overview)**
- 🟢 Green = everything is fine
- 🟡 Yellow = warning, monitor
- 🔴 Red = URGENT, investigate now

### **Traffic & Errors (Detailed View)**
- Spike up + flat error = volume aumentou, tudo ok
- Spike up + spike error = problema, não escala bem
- Flat + spike error = qualidade degradou, bug provável

### **Latency (p95 vs p99)**
- p99 >> p95 = outliers (GC, slow queries, network)
- p99 ≈ p95 = consistent, no outliers

### **Saturation (Resource Usage)**
- Green: comfortável, no bottleneck
- Yellow: warning, plan expansion
- Red: urgent, requests enfileiradas, latencia sobe

### **SLO Compliance**
- **Availability**: % of time at least one app up (1h window)
- **SLO Compliance**: error rate vs threshold (PASS/FAIL card)
- **Error Budget**: remaining % ("% of allowed errors left")
- **SLO Status by App**: table, color by app error rate

### **Drill-Down (Analysis)**
- **Endpoints by Status**: top 10 by throughput, color coded by HTTP status
- **Error Endpoints (5xx)**: top 15 returning 500, sorted by error rate
- **Responses by Status Code**: series by 2xx/3xx/4xx/5xx (% of total)

---

## 5️⃣ Common Patterns

### Pattern: "Cache is stale"
- ✗ Latency high BUT Saturation low → dependency issue
- ✓ Check Trace Topology → find slow downstream service
- ✓ Restart cache or increase TTL

### Pattern: "GC pauses"
- ✓ Heap > 85% → Young gen full, Major GC imminent
- ✓ p99 >> p95 → confirm GC pauses in GC logs
- ✓ Fix: increase heap or reduce object allocation

### Pattern: "Memory leak"
- ✓ Heap climbing steadily, never drops → leak likely
- ✓ Check app logs for "not closing resources"
- ✓ Use JProfiler/YourKit locally to find leak

### Pattern: "Traffic spike"
- ✓ Traffic up, Error rate flat → expected scaling
- ✓ Traffic up, Error rate up → can't handle load
- ✓ Traffic up, Latency up → cpu/memory saturated
- ✓ Solution: scale horizontal, or optimize code

---

## 6️⃣ Color Codes

| Color | Meaning | Action |
|-------|---------|--------|
| 🟢 Green | Healthy | Monitor, no action needed |
| 🟡 Yellow | Warning | Watch trends, prep for response |
| 🔴 Red | Critical | Investigate immediately, fix ASAP |
| ⚪ Gray | No data | Check if app running & scraping |

---

## 7️⃣ Links & Integration

| Need | Dashboard | Filter Suggestions |
|------|-----------|-------------------|
| See error details | **Error Endpoints (5xx)** | Select Business App |
| Follow the call chain | **Trace Topology** → UI Link | Click error status in drill-down |
| Read logs | **Logs JSON Loki** | Copy correlationId from trace |
| Check infra | **Platform Infrastructure Health** | No filter needed |

---

## 8️⃣ SLO Cheat Sheet

```
Error budget calculation (per measurement window):
  Budget% = 100% - (error_rate%)
  
Interpretation:
  Budget > 75% → safe to deploy
  Budget 50-75% → ok to deploy, but be careful
  Budget 25-50% → only critical fixes
  Budget < 25% → FREEZE, must fix errors first
```

---

## 9️⃣ When Things Break (Checklist)

- [ ] App spinning? Check **Business Targets Up** (Overview)
- [ ] Prometheus scraping? Go to `http://localhost:9090` → Targets
- [ ] Metrics old? Check Prometheus **Scrape Duration** (Platform Health)
- [ ] Time window correct? Adjust **Range Picker** (top right)
- [ ] Filter too narrow? Check Business App + Platform Component filters
- [ ] Metric missing? Restart app, wait 30s, refresh dashboard
- [ ] Still blank? Run: `curl http://localhost:9005/actuator/prometheus`

---

## 🔗 Novo: Links Automáticos para Trace Topology

| Painel | Como Usar | Destino |
|--------|-----------|---------|
| Table "Error Endpoints (5xx)" | Right-click row | Trace Topology com context |
| Series "Responses by Status Code" | Click 5xx line (red/bold) | Trace Topology ERROR level |
| Table "SLO Status by App" | Click app name (red) | Trace Topology filtered |
| Card "Quick Navigation" | Click any of 4 buttons | Trace/Logs/Timeline dashboards |

**Pro tip**: Every link auto-fills relevant filters (app, method, status, error level)

1. **Bookmark this dashboard**: you'll use it daily
2. **Set alerts**: high error rate, SLO breach, saturation > 80%
3. **Add to mobile home**: Grafana supports mobile dashboards
4. **Export to PDF weekly**: post to team channel as status report
5. **Use time range shortcuts**: 🔥 "Last 24h" to see trends
6. **Clone dashboard** for experiment: e.g., test app performance before deploy
7. **Combine with logs**: open Trace Topology in split-screen with Logs Loki

---

**Last Updated**: 2026-05-11  
**For full guide**: see `APP-OBSERVABILITY-GUIDE.md`

