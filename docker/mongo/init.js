// Idempotent seed for local development bootstrap
var dbRef = db.getSiblingDB("hexarch");
dbRef.getCollection("bootstrap").updateOne(
  { _id: "local-seed" },
  { $set: { updatedAt: new Date(), source: "docker-init" } },
  { upsert: true }
);

// --- CONFIGURAÇÃO PARA O MÓDULO DE SEGURANÇA ---
var dbSecurity = db.getSiblingDB("security");

// 1. Criar o Tenant inicial (Essencial para o filtro passar)
dbSecurity.getCollection("tenants").updateOne(
  { slug: "tenant-a" },
  {
    $set: {
      id: "019d87dc-b820-7082-bc93-c93f0ce574f4",
      name: "Meu Primeiro Cliente",
      active: true
    }
  },
  { upsert: true }
);

// 2. Criar o Client inicial (Essencial para teste do desenvolvedor)
dbSecurity.getCollection("clients").updateOne(
    { clientId: "meu-app-teste" },
    {
        $set: {
            tenantId: "019d87dc-b820-7082-bc93-c93f0ce574f4",
            clientSecret: "$argon2id$v=19$m=65536,t=3,p=1$CcgA1LV1WOB1sKGsUElhew$0Rpw7pSPi1TOLqlQr1Wm1SmMwVTcUMScVqiG40Ozsxg", //segredo123
            redirectUris: ["https://oidcdebugger.com/debug"],
            scopes: ["profile", "read", "openid"],
            grantTypes: ["authorization_code", "refresh_token", "client_credentials"],
            active: true
        }
    },
    { upsert: true }
);

// 3. Criar o User inicial (Essencial para teste do desenvolvedor)
dbSecurity.getCollection("users").updateOne(
    { username: "teste" },
    {
        $set: {
            tenantId: "019d87dc-b820-7082-bc93-c93f0ce574f4",
            password: "$argon2id$v=19$m=65536,t=3,p=1$ohQYQC8IozK2nvLmldlmKA$B3uClscAfq1cNe7nW4if9gOGPrESZu+YyDZ+u4JrJN8", //teste
            roles: ["ROLE_USER"],
            active: true
        }
    },
    { upsert: true }
);



// --- CONFIGURAÇÃO PARA O MÓDULO DE DISTRIBUIÇÃO DE QUESTIONARIO ---
var dbOrderQuestionnaire = db.getSiblingDB("orderquestionnaire");

var now = new Date();
var auditInfo = {
    created_by: {
        id: "usr-seed",
        reference_code: "seed_user",
        name: "Seed User",
        email: "seed.user@acme.com"
    },
    created_at: now,
    updated_by: {
        id: "usr-seed",
        reference_code: "seed_user",
        name: "Seed User",
        email: "seed.user@acme.com"
    },
    updated_at: now
};

// 1. Canais de distribuicao em estados diferentes
dbOrderQuestionnaire.getCollection("channel_distributions").updateOne(
    { id: "mobile_acmeapp" },
    {
        $set: {
            reference_code: "mobile_acmeapp",
            name: "Canal de Vendas Mobile",
            active: true
        }
    },
    { upsert: true }
);

dbOrderQuestionnaire.getCollection("channel_distributions").updateOne(
    { id: "store_acme" },
    {
        $set: {
            reference_code: "store_acme",
            name: "Canal Loja Fisica",
            active: false
        }
    },
    { upsert: true }
);

// 2. Jornadas de distribuicao em estados diferentes
dbOrderQuestionnaire.getCollection("journey_distributions").updateOne(
    { id: "journey_vendaavulsaacme" },
    {
        $set: {
            reference_code: "journey_vendaavulsaacme",
            name: "Jornada de Venda Avulsa ACME",
            active: true
        }
    },
    { upsert: true }
);

dbOrderQuestionnaire.getCollection("journey_distributions").updateOne(
    { id: "journey_retencao" },
    {
        $set: {
            reference_code: "journey_retencao",
            name: "Jornada de Retencao",
            active: false
        }
    },
    { upsert: true }
);

// 3. Questoes em estados diferentes
dbOrderQuestionnaire.getCollection("questions").updateOne(
    { _id: "q_name" },
    {
        $set: {
            label: "Qual o seu nome?",
            status: "ACTIVE",
            sales_item_reference_code: "prd001",
            audit_info: auditInfo
        }
    },
    { upsert: true }
);

dbOrderQuestionnaire.getCollection("questions").updateOne(
    { _id: "q_age" },
    {
        $set: {
            label: "Qual a sua idade?",
            status: "DRAFT",
            sales_item_reference_code: "prd001",
            audit_info: auditInfo
        }
    },
    { upsert: true }
);

dbOrderQuestionnaire.getCollection("questions").updateOne(
    { _id: "q_consent" },
    {
        $set: {
            label: "Aceita compartilhar dados?",
            status: "INACTIVE",
            sales_item_reference_code: "prd002",
            audit_info: auditInfo
        }
    },
    { upsert: true }
);

// 4. Questionarios em estados diferentes
dbOrderQuestionnaire.getCollection("questionnaires").updateOne(
    { id: "questionnaire_checkout" },
    {
        $set: {
            channel_distribution_id: "mobile_acmeapp",
            journey_distribution_id: "journey_vendaavulsaacme",
            description: "Questionario de Checkout",
            status: "ACTIVE",
            configured_questions: [],
            audit_info: auditInfo
        }
    },
    { upsert: true }
);

dbOrderQuestionnaire.getCollection("questionnaires").updateOne(
    { id: "questionnaire_onboarding" },
    {
        $set: {
            channel_distribution_id: "store_acme",
            journey_distribution_id: "journey_retencao",
            description: "Questionario de Onboarding",
            status: "DRAFT",
            configured_questions: [],
            audit_info: auditInfo
        }
    },
    { upsert: true }
);
