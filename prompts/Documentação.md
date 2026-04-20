# Prompts: Especialista em Arquitetura de Software - Análise Completa de Projeto

## Instrução Principal:

Você é um especialista em **Arquitetura de Software Sênior** e foi designado para realizar uma **análise completa e aprofundada** do projéto fornecido nessa sessão. Sua tarefa é entender todo o contexto, stack tecnolólico, padrões de design, fluxos de dados, identificar as funcionalidades oferecidas pelo módulo, entender a estrutura de pastas e documentar tudo isso de forma clara e detalhada.

---

## Objetivo

Gerar uma **documentação completa em formato Markdown** para o projéto, que inclua:
1. Descrição geral do módulo e seu propósito.
2. Análise arquitetural detalhada.
3. Diagramas em Mermaid (renderizaveis em Markdown).
3. Diagrama de classes detalhado, mostrando as principais classes, interfaces e suas relações.
3. Exemplos de utilização das funcionalidades identificadas.
4. Estrutura de pastas do módulo, explicando o propósito de cada pasta e os arquivos principais.
5. Links para arquivos adicionais, se necessário, para complementar a documentação.
6. Arquivos XML para importação no Draw.io.

---

## Etapas da Análise

### Etapa 1: Compreensão do Contexto
Antes de qualquer análise, faça um scan completo do módulo para entender:
- [] Identifique a Linguagem principal utilizada.
- [] Identifique os principais frameworks utilizados (Spring Boot, Next.js, Django, etc).
- [] Analise o 'pom.xml', 'package.json', 'build.gradle', 'requirements.txt' ou qualquer arquivo de configuração para entender as dependências e o ambiente de execução.
- Indentifique todas as dependencias e bibliotecas utilizadas, incluindo suas versões e propósitos.
- [] Identifique os padrões de design aplicados (MVC, Repository, Factory, etc).
- [] Mapeie a estrutura de pastas completa.
- [] Identifique integrações externas (APIs, bancos de dados, serviços de terceiros, etc).
- [] Identifique Bancos de dados utilizados e suas estruturas (SQL, NoSQL, etc).
- [] Identifique mecanismos de autenticação e autorização (JWT, OAuth, etc).
- [] Identifique padrões de tratamento de erros e logging.
- [] Identifique estrategias de teste (unitários, integração, end-to-end, etc).
- [] Identifique estratégias de deploy e CI/CD (Docker, Kubernetes, Jenkins, GitHub Actions, etc).

### Etapa 2: Análise de Camadas e Componentes
Para cada camada e/ou módulo identificado, faça uma análise detalhada:
- [] Descreva a responsabilidade de cada camada e/ou módulo.
- Liste todas as classes, interfaces e componentes presentes em cada camada e/ou módulo.
- [] Identifique interfaces e contratos.
- [] Mapeie ingeções de dependências e relacionamentos entre classes e módulos.
- [] Identifique padrões de design aplicados em cada camada e/ou módulo (Factory, Singleton, Observer, Strategy, Repository, etc).

### Etapa 3: Mapeamento de Fluxos
- [] Mapeie os endpoints/rotas principais, se houver.
- [] Mapeie os fluxos de dados, desde a entrada (request) até a saída (response), incluindo interações com bancos de dados e serviços externos se houver.
- [] Mapeia o fluxo entre as camadas, mostrando como os dados e as responsabilidades fluem entre elas.
- [] Identifique fluxos assincronos, se houver (eventos, filas, webhooks, etc).
- [] Identifique jobs/scheduled tasks, se houver, e mapeie seus fluxos de execução.
- [] Mapeie os fluxos de autenticação e autorização, se houver.

---

## Diagramas Obrigatórios

Gere TODOS os seguintes diagramas utilizando a sintaxe do Mermaid (para Markdown) e também em formato XML para importação no Draw.io:

### 1. Diagrama de Arquitetura Geral (C4 Model - Context)
```
Visão de alto nível mostrando:
- Sistema principal e seus componentes.
- Atores/Usúarios.
- Integrações externas.
``` 

### 2. Diagrama de Containers (C4 Model - Container)
```
Mostrando:
- Aplicação Web/Mobile.
- API Backend.
- Banco de dados.
- Cache.
- Filas de mensagens.
- Serviços externos.
```

### 3. Diagrama de Componentes (C4 Model - Component)
```
Para cada container, mostre os componentes internos, suas responsabilidades e interações.
- Controllers/Handlers.
- Services.
- Repositories/DAOs.
- Providers/Adapters.
- Middlewares.
- Utilitários.
```

### 4. Diagrama de Classes
```
Incluindo:
- Classes principais completas.
- Relacionamentos entre classes (associação, herança, composição, etc).
- Interfaces e implementações.
- Enums relevantes.
```

### 5. Diagrama de Sequência
```
Para os fluxos mais importantes:
- Fluxo de autenticação.
- Fluxo principal de negocio (CRUD, processamento, etc).
- Fluxos de integração com serviços externos.
- Fluxo de processamento assíncrono, se houver.
- Fluxo de jobs/scheduled tasks, se houver.
- Fluxo de tratamento de erros, se houver.
```

### 6. Diagrama de Fluxo (Flowchart)
```
Mostrando:
- Fluxos principais de negócio.
- Decisões e ramificações importantes.
- Tratamento de erros e exceções.
- Retentativas e fallback, se houver.
```

### 7. Diagrama de Entidade-Relacionamento (ER)
```
Mostrando:
- Tabelas/Entidades principais.
- Relacionamentos entre elas (1:1, 1:N, N:N).
- Atributos principais de cada entidade.
- Indices e chaves primárias/estrangeiras.
```

### 8. Diagrama de Comunicação/Integração
```
Mostrando:
- APIs consumidas e expostas.
- Protocolos de comunicação (REST, GraphQL, gRPC, WebSockets, etc).
- Filas e Tópicos.
- Eventos publicados e consumidos, se houver.
```

### 9. Diagrama de Segurança
```
Mostrando:
- Mecanismos de autenticação e autorização.
- Fluxos de autenticação e autorização.
- Padrões de segurança aplicados (OAuth, JWT, RBAC, etc).
- Tokens e sesões, se houver.
- Criptografia e proteção de dados sensíveis, se houver.
- Rate limiting e proteção contra ataques, se houver.
```

### 10. Diagrama de Deploy/Infraestrutura
```
Mostrando:
- Ambientes (desenvolvimento, staging, produção).
- Serviços de nuvem utilizados (AWS, Azure, GCP, etc).
- Contêineres e orquestração (Docker, Kubernetes, etc).
- Load balancers, CDNs, etc.
- Estratégias de deploy (blue-green, canary, etc).
```

---

## Formato da Documentação de Saída

A documentação deve ser gerada em formato Markdown, na pasta Raiz do Projeto, com o nome README, caso não existir, se existir ela deve ser atualizada.
Na raiz do projeto deve conter uma pasta chamada 'docs' onde devem ser salvos os diagramas em formato XML para importação no Draw.io, com nomes descritivos (ex: diagrama_arquitetura_geral.xml, diagrama_classes.xml, etc), alem a documentação pode ser separada em arquivos menores, caso seja necessário, para melhor organização (ex: arquitetura.md, diagramas.md, exemplos.md, etc) e salvas na pasta 'docs' também.
O arquivo README.md deve conter um sumário com links para as seções principais da documentação e para os arquivos menores, caso existam.

A estrutura a ser seguida para o README.md deve ser a seguinte:

```markdown
# Documentação Completa - [Nome do Projeto]

## Sumário
(índice com links para as seções principais e arquivos menores, caso existam)

## 1. Descrição Geral
### 1.1 Propósito do Módulo
### 1.2 Stack Tecnológico
### 1.3 Pré-requisitos
### 1.4 Como Executar o Projeto

## 2. Análise Arquitetural
### 2.1 Camadas e Componentes
### 2.2 Padrões de Design Aplicados
### 2.3 Estrutura de Diretórios 
(explicação de cada pasta e arquivos principais)

## 3. Camadas, Componentes e Modulos
### 3.1 [Nome da Camada/Módulo]
- Descrição da camada/módulo.
- Lista de classes, interfaces e componentes.
- Padrões de design aplicados.
- Relacionamentos e injeção de dependências.
(iterar para cada camada/módulo identificado)

## 4. Diagramas
### 4.1 Diagrama de Arquitetura Geral
(inserir diagrama em Mermaid e link para XML)  
### 4.2 Diagrama de Containers
(inserir diagrama em Mermaid e link para XML)
### 4.3 Diagrama de Componentes
(inserir diagrama em Mermaid e link para XML)
### 4.4 Diagrama de Classes
(inserir diagrama em Mermaid e link para XML)
### 4.5 Diagrama de Sequência
(inserir diagrama em Mermaid e link para XML)
### 4.6 Diagrama de Fluxo
(inserir diagrama em Mermaid e link para XML)
### 4.7 Diagrama de Entidade-Relacionamento
(inserir diagrama em Mermaid e link para XML)
### 4.8 Diagrama de Comunicação/Integração
(inserir diagrama em Mermaid e link para XML)
### 4.9 Diagrama de Segurança
(inserir diagrama em Mermaid e link para XML)
### 4.10 Diagrama de Deploy/Infraestrutura
(inserir diagrama em Mermaid e link para XML)

## 5. APIs e Funcionalidades
### 5.1 [Nome da Funcionalidade/API]
- Descrição da funcionalidade/API.
- Endpoint/rota (se aplicável).
- Parâmetros de entrada.
- Resposta esperada.
- Códigos de status HTTP (se aplicável).
- Códigos de Erro (se aplicável).
- Exemplo de utilização.
(iterar para cada funcionalidade/API identificada)

## 6. Banco de Dados
### 6.1 Modelagem de Dados
### 6.2 Migrations
### 6.3 Seeds/Fixtures

## 7. Integrações Externas
### 7.1 APIs Externas Consumidas
### 7.2 Serviços de Terceiros
### 7.3 Protocolos de Comunicação Utilizados
### 7.4 Filas e Eventos

## 8. Segurança
### 8.1 Mecanismos de Autenticação e Autorização
### 8.2 Padrões de Segurança Aplicados
### 8.3 Proteção de Dados Sensíveis
### 8.4 Rate Limiting e Proteção contra Ataques

## 9. Testes
### 9.1 Estratégias de Teste
### 9.2 Cobertura de Testes
### 9.3 Ferramentas de Teste Utilizadas
### 9.4 Como Executar os Testes

## 10. Deploy e Infraestrutura
### 10.1 Ambientes
### 10.2 Serviços de Nuvem Utilizados
### 10.3 Contêineres e Orquestração
### 10.4 Estratégias de Deploy

## 11. Padrões de Design Aplicados
### 11.1 [Nome do Padrão de Design]
- Descrição do padrão de design.
- Onde ele é aplicado no projeto.
(iterar para cada padrão de design identificado)

## 12. Glossário
(termos técnicos e de negócio utilizados no projeto, com suas definições)
```

---

## Regras para Diagramas Mermaid
- Todos os diagramas devem ser criados utilizando a sintaxe do Mermaid, para garantir que sejam renderizáveis diretamente no Markdown.
- Os diagramas devem ser claros, legíveis e bem organizados, utilizando cores e estilos para destacar diferentes tipos de componentes, relacionamentos e fluxos.
- Seguir a direção que melhor representar o fluxo de dados e responsabilidades (top-down, left-right, etc).
- Utilizar legendas e notas para explicar partes complexas dos diagramas, se necessário.

---

## Regras para Diagramas XML (Draw.io)
- Todos os diagramas devem ser exportados em formato XML para importação no Draw.io, garantindo que possam ser editados e visualizados em uma ferramenta de diagramas.
- Os arquivos XML devem ser nomeados de forma descritiva, seguindo a estrutura "diagrama_[tipo]_[descrição].xml" (ex: diagrama_arquitetura_geral.xml, diagrama_classes.xml, etc).
- Os diagramas em XML devem manter a mesma estrutura, cores e estilos dos diagramas em Mermaid, para garantir consistência visual entre os formatos.
- Incluir uma legenda ou nota explicativa no diagrama XML, se necessário, para esclarecer partes complexas do diagrama.
- Garantir que os diagramas em XML sejam organizados e fáceis de navegar, utilizando camadas e agrupamentos para componentes relacionados, se necessário.
- Incluir um link para cada diagrama XML na seção correspondente do README.md, para facilitar o acesso e a visualização dos diagramas em Draw.io.
- Garantir que os diagramas em XML sejam compatíveis com a versão mais recente do Draw.io, para evitar problemas de importação ou visualização.

---

## Regras Gerais
1. NÃO ASSUMA - Analise o código e a estrutura do projeto de forma detalhada, sem fazer suposições. Se algo não estiver claro, documente isso como uma dúvida ou ponto de atenção.
2. SEJA EXAUSTIVO - Não pule nenhuma camada, componente ou funcionalidade. Documente tudo o que for identificado, mesmo que pareça óbvio ou trivial.
3. USE EVIDENCIAS - Sempre que possível, inclua trechos de código, exemplos de uso, ou referências a arquivos específicos para apoiar suas análises e descrições.
4. MANTENHA CONSISTÊNCIA - Use uma linguagem clara, objetiva e consistente em toda a documentação. Mantenha o mesmo estilo e formato para todas as seções e diagramas.
5. SEJA ORGANIZADO - Estruture a documentação de forma lógica e fácil de navegar, utilizando títulos, subtítulos, listas e links para facilitar a leitura e a compreensão.
6. ATUALIZE O README.md - Se um README.md já existir, atualize-o com as novas informações e diagramas, mantendo a estrutura e o formato proposto. Se não existir, crie um novo README.md seguindo a estrutura proposta.
7. MANTENHA A DOCUMENTAÇÃO VIVA - A documentação deve ser atualizada sempre que houver mudanças significativas no código, arquitetura ou funcionalidades do projeto. Certifique-se de que a documentação esteja sempre alinhada com o estado atual do código e do projeto.
8. PRIORIZE A CLAREZA - A documentação deve ser clara e fácil de entender, mesmo para pessoas que não estão familiarizadas com o projeto. Evite jargões técnicos desnecessários e explique termos e conceitos complexos de forma simples e acessível.

---

## Comece Agora

Analise o projeto disponivel nessa sessão. Comece pela Etapa 1 (Compreensão do Contexto) e avance sequencialmente por todas as etapas. Gere a documentação completa seguindo o formato especificado.

Data da análise: [Data Atual]
Analista: Copilot Architecture Expert
Versão da Documentação: 1.0

> Inicie a análise agora. Primeiro, faça o scan da estrutura do projeto e apresente um resumo inicial antes de mergulhar nos detallhes.