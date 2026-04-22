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
var dbOderQuestionnaire = db.getSiblingDB("oderquestionnaire");

// 1. Criar o Canal inicial (Essencial para teste do desenvolvedor)
dbSecurity.getCollection("channel_distributions").updateOne(
    { id: "mobile_acmeapp" },
    {
        $set: {
            referenceCode: "mobile_acmeapp",
            name: "Canal de Vendas Mobile",
            active: true
        }
    },
    { upsert: true }
);

// 2. Criar o Jornada de Venda inicial (Essencial para teste do desenvolvedor)
dbSecurity.getCollection("journey_distributions").updateOne(
    { id: "journey_vendaavulsaacme" },
    {
        $set: {
            referenceCode: "journey_vendaavulsaacme",
            name: "Jornada de Venda Avulsa ACME",
            active: true
        }
    },
    { upsert: true }
);