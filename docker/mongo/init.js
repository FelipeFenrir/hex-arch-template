// --- CONFIGURAÇÃO PARA O MÓDULO DE SEGURANÇA ---
var dbSecurity = db.getSiblingDB("security");

// 1. Criar o Tenant inicial (Essencial para o filtro passar)
dbSecurity.getCollection("tenants").updateOne(
  {
    _id: "019d87dc-b820-7082-bc93-c93f0ce574f4",
    slug: "tenant-a"
  },
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
    {
        _id: "019dc2c8-a3e3-721b-8d56-974c94406724",
        clientId: "meu-app-teste"
    },
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
    {
        _id: "019dc2c9-5cbe-7bc9-a6c0-5d9aaeb4693b",
        username: "teste"
    },
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
        id: "019dff07-5f02-70d4-8680-f8dc34fd5fb9",
        reference_code: "seed_user",
        name: "Seed User",
        email: "seed.user@acme.com"
    },
    created_at: now,
    updated_by: {
        id: "019dff07-5f02-70d4-8680-f8dc34fd5fb9",
        reference_code: "seed_user",
        name: "Seed User",
        email: "seed.user@acme.com"
    },
    updated_at: now
};

// 1. Canais de distribuicao em estados diferentes
dbOrderQuestionnaire.getCollection("channel_distributions").updateOne(
    { _id: "mobile_acmeapp" },
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
    { _id: "store_acme" },
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
    { _id: "journey_vendaavulsaacme" },
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
    { _id: "journey_retencao" },
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
    { _id: "questionnaire_checkout|mobile_acmeapp|journey_vendaavulsaacme" },
    {
        $set: {
            id: "questionnaire_checkout",
            channel_distribution_id: "mobile_acmeapp",
            journey_distribution_id: "journey_vendaavulsaacme",
            description: "Questionario de Checkout",
            status: "ACTIVE",
            questions_count: 3,
            audit_info: auditInfo
        }
    },
    { upsert: true }
);

dbOrderQuestionnaire.getCollection("questionnaires").updateOne(
    { _id: "questionnaire_onboarding|store_acme|journey_retencao" },
    {
        $set: {
            id: "questionnaire_onboarding",
            channel_distribution_id: "store_acme",
            journey_distribution_id: "journey_retencao",
            description: "Questionario de Onboarding",
            status: "DRAFT",
            questions_count: 1,
            audit_info: auditInfo
        }
    },
    { upsert: true }
);

// 5. Relacao questionnaire_questions
//    Checkout: q_name (1) → q_consent (2) → q_age (3, condicional a q_consent = "sim")
var checkoutDocId = "questionnaire_checkout|mobile_acmeapp|journey_vendaavulsaacme";

dbOrderQuestionnaire.getCollection("questionnaire_questions").updateOne(
    { _id: checkoutDocId + "|q_name" },
    {
        $set: {
            questionnaire_document_id: checkoutDocId,
            questionnaire_id: "questionnaire_checkout",
            channel_distribution_id: "mobile_acmeapp",
            journey_distribution_id: "journey_vendaavulsaacme",
            question_id: "q_name",
            answer_configuration: {
                _class: "com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerTextStrategy",
                regexPattern: null,
                customErrorMessage: null
            },
            root_condition: null,
            order: 1
        }
    },
    { upsert: true }
);

dbOrderQuestionnaire.getCollection("questionnaire_questions").updateOne(
    { _id: checkoutDocId + "|q_consent" },
    {
        $set: {
            questionnaire_document_id: checkoutDocId,
            questionnaire_id: "questionnaire_checkout",
            channel_distribution_id: "mobile_acmeapp",
            journey_distribution_id: "journey_vendaavulsaacme",
            question_id: "q_consent",
            answer_configuration: {
                _class: "com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerOptionListStrategy",
                answerOptions: [
                    { value: "sim", label: "Sim" },
                    { value: "nao", label: "Nao" }
                ],
                customErrorMessage: null
            },
            root_condition: null,
            order: 2
        }
    },
    { upsert: true }
);

dbOrderQuestionnaire.getCollection("questionnaire_questions").updateOne(
    { _id: checkoutDocId + "|q_age" },
    {
        $set: {
            questionnaire_document_id: checkoutDocId,
            questionnaire_id: "questionnaire_checkout",
            channel_distribution_id: "mobile_acmeapp",
            journey_distribution_id: "journey_vendaavulsaacme",
            question_id: "q_age",
            answer_configuration: {
                _class: "com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerNumberStrategy",
                min: 1.0,
                max: 120.0,
                step: null,
                allowedDecimal: false,
                allowedNegative: false,
                customErrorMessage: null
            },
            root_condition: {
                _class: "com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition",
                questionRootCode: "q_consent",
                expectedValue: "sim"
            },
            order: 3
        }
    },
    { upsert: true }
);

//    Onboarding: q_name (1)
var onboardingDocId = "questionnaire_onboarding|store_acme|journey_retencao";

dbOrderQuestionnaire.getCollection("questionnaire_questions").updateOne(
    { _id: onboardingDocId + "|q_name" },
    {
        $set: {
            questionnaire_document_id: onboardingDocId,
            questionnaire_id: "questionnaire_onboarding",
            channel_distribution_id: "store_acme",
            journey_distribution_id: "journey_retencao",
            question_id: "q_name",
            answer_configuration: {
                _class: "com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerTextStrategy",
                regexPattern: null,
                customErrorMessage: null
            },
            root_condition: null,
            order: 1
        }
    },
    { upsert: true }
);

