# scripts/

Utilitários locais para desenvolvimento e testes manuais contra o MiniStack.

## create_test_resources.py

Cria recursos de exemplo no MiniStack para validar a UI do AWS Manager.

```bash
py -3 scripts/create_test_resources.py
```

Cria:
- S3: `test-bucket-1`, `test-bucket-2`, `logs-archive`
- SQS: `task-queue`, `notifications`, `dlq-messages`
- SNS: `order-events`, `alerts`, `notifications`

## verify_resources.py

Verifica se os recursos S3, SQS e SNS estão acessíveis via AWS Manager backend.
Útil para confirmar que a detecção automática de endpoint está funcionando.

```bash
py -3 scripts/verify_resources.py
```

> **Nota:** esses scripts são utilitários locais, não fazem parte do build nem do CI.

