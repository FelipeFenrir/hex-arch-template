# AWS Manager

Gerenciador de servicos AWS locais, organizado por servico.

## Funcionalidades
- **SQS**: monitoramento de filas, criacao de fila e envio de mensagens em batch.
- **SNS**: monitoramento de topicos, criacao de topico, publicacao de mensagens e assinatura SNS → SQS.

## Estrutura
```
aws_manager/
  __init__.py          # factory create_app() — registra todos os blueprints
  clients.py           # internos compartilhados (_build_client, _resolve_endpoint)
  config.py            # configuracoes e flags de runtime
  utils.py             # utilitarios (parse JSON, normalizacao de URL)
  services.py          # DEPRECATED — re-exporta sqs.services + sns.services
  sqs/
    clients.py         # build_sqs_client
    services.py        # list_available_queues, get_queue_info_by_name, filter_messages …
    web/
      pages.py         # rotas HTML: /monitor, /sqs/queues/create, /sqs/messages/send
      api.py           # rotas JSON: /monitor/queues, /monitor/messages …
  sns/
    clients.py         # build_sns_client
    services.py        # list_available_topics, get_topic_arn_by_name, list_topic_subscriptions
    web/
      pages.py         # rotas HTML: /sns, /sns/topics/create, /sns/messages/publish …
      api.py           # rotas JSON: /sns/topics, /sns/subscriptions, /api/sns/subscriptions/sqs
  web/
    pages.py           # home_blueprint — apenas rota raiz /
    monitor_api.py     # DEPRECATED — removido do app
```

## Executar localmente
```powershell
Push-Location "D:\Projetos\hex-arch-template\docker\aws-manager"
py -3 app.py
Pop-Location
```

## Smoke test rapido
```powershell
Push-Location "D:\Projetos\hex-arch-template\docker\aws-manager"
py -3 smoke_test.py
Pop-Location
```
