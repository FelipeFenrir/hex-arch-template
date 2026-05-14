# scripts/

Utilitários locais para desenvolvimento e testes manuais contra o MiniStack.

> A criacao de recursos AWS locais agora e responsabilidade do Terraform em `docker/ministack/terraform`.

## verify_resources.py

Verifica se os recursos S3, SQS e SNS estão acessíveis via AWS Manager backend.
Útil para confirmar que a detecção automática de endpoint está funcionando.

```bash
py -3 scripts/verify_resources.py
```

> **Nota:** esses scripts são utilitários locais, não fazem parte do build nem do CI.

