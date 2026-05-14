export type ServiceStat = {
  status: string;
  resources: Record<string, number>;
};

export type StatsResponse = {
  services: Record<string, ServiceStat>;
  total_resources: number;
  uptime_seconds: number;
};

export type EndpointRoute = {
  path: string;
  methods: string[];
  endpoint: string;
};

export type MinistackHealthResponse = {
  online: boolean;
  services: Record<string, boolean>;
  checked_at: string;
  source?: string;
  error?: string;
};

export class ApiError extends Error {
  status: number;
  payload: unknown;

  constructor(message: string, status: number, payload: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

async function fetchJSON<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, init);
  if (!response.ok) {
    let payload: unknown = null;
    let detail = `${response.status}: ${response.statusText}`;
    try {
      const body = (await response.json()) as { error?: string };
      payload = body;
      if (body?.error) {
        detail = `${response.status}: ${body.error}`;
      }
    } catch {
      // Keep fallback message when response is not JSON.
    }
    throw new ApiError(detail, response.status, payload);
  }
  return response.json() as Promise<T>;
}

export function getServiceTotal(service: ServiceStat): number {
  return Object.values(service.resources).reduce((sum, count) => sum + count, 0);
}

export async function fetchStats(): Promise<StatsResponse> {
  return fetchJSON<StatsResponse>("/api/stats");
}

export async function fetchEndpoints(): Promise<{ routes: EndpointRoute[] }> {
  return fetchJSON<{ routes: EndpointRoute[] }>("/api/endpoints");
}

export async function fetchMinistackHealth(): Promise<MinistackHealthResponse> {
  return fetchJSON<MinistackHealthResponse>("/api/ministack/health");
}

export async function fetchResources(service: string): Promise<{ service: string; resources: Record<string, Array<Record<string, unknown>>> }> {
  return fetchJSON(`/api/resources/${encodeURIComponent(service)}`);
}

export async function fetchResourceDetail(service: string, type: string, id: string): Promise<{ detail: unknown }> {
  return fetchJSON(`/api/resources/${encodeURIComponent(service)}/${encodeURIComponent(type)}/${encodeURIComponent(id)}`);
}

export async function fetchS3Buckets(): Promise<{ buckets: Array<Record<string, unknown>> }> {
  return fetchJSON("/api/s3/buckets");
}

export async function createS3Bucket(payload: Record<string, unknown>) {
  return fetchJSON("/api/s3/buckets", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export async function updateS3Bucket(payload: Record<string, unknown>) {
  return fetchJSON("/api/s3/buckets", { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export async function deleteS3Bucket(payload: Record<string, unknown>) {
  return fetchJSON("/api/s3/buckets", { method: "DELETE", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export async function fetchSqsQueues(): Promise<{ queues: Array<{ name: string; url: string }> }> {
  return fetchJSON("/api/sqs/queues");
}

export async function createSqsQueue(payload: Record<string, unknown>) {
  return fetchJSON("/api/sqs/queues", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export async function deleteSqsQueue(name: string) {
  return fetchJSON(`/api/sqs/queues/${encodeURIComponent(name)}`, { method: "DELETE" });
}

export async function deleteSqsQueueForced(name: string) {
  return fetchJSON(`/api/sqs/queues/${encodeURIComponent(name)}?force=true`, { method: "DELETE" });
}

export async function fetchSqsQueueSummary(name: string) {
  return fetchJSON<{
    name: string;
    visibilityTimeout: number;
    messageRetentionPeriod: number;
    visibleMessages: number;
    inflightMessages: number;
    hasActiveMessages: boolean;
  }>(`/api/sqs/queues/${encodeURIComponent(name)}/summary`);
}

export async function updateSqsQueue(name: string, payload: Record<string, unknown>) {
  return fetchJSON(`/api/sqs/queues/${encodeURIComponent(name)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function sendSqsMessage(name: string, payload: Record<string, unknown>) {
  return fetchJSON(`/api/sqs/queues/${encodeURIComponent(name)}/messages`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export async function fetchSnsTopics(): Promise<{ topics: Array<{ name: string; arn: string }> }> {
  return fetchJSON("/api/sns/topics");
}

export async function createSnsTopic(payload: Record<string, unknown>) {
  return fetchJSON("/api/sns/topics", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export async function deleteSnsTopic(name: string) {
  return fetchJSON(`/api/sns/topics/${encodeURIComponent(name)}`, { method: "DELETE" });
}

export async function deleteSnsTopicForced(name: string) {
  return fetchJSON(`/api/sns/topics/${encodeURIComponent(name)}?force=true`, { method: "DELETE" });
}

export async function fetchSnsTopicSummary(name: string) {
  return fetchJSON<{
    name: string;
    displayName: string;
    subscriptionsCount: number;
    hasActiveSubscriptions: boolean;
  }>(`/api/sns/topics/${encodeURIComponent(name)}/summary`);
}

export async function updateSnsTopic(name: string, payload: Record<string, unknown>) {
  return fetchJSON(`/api/sns/topics/${encodeURIComponent(name)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function publishSnsMessage(name: string, payload: Record<string, unknown>) {
  return fetchJSON(`/api/sns/topics/${encodeURIComponent(name)}/publish`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export async function subscribeSqsToSns(payload: Record<string, unknown>) {
  return fetchJSON("/api/sns/subscriptions/sqs", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export type DynamoTable = {
  name: string;
  status: string;
  itemCount: number;
  billingMode: string;
  hashKey: string;
  readCapacity: number;
  writeCapacity: number;
};

export async function fetchDynamodbTables(): Promise<{ tables: DynamoTable[] }> {
  return fetchJSON("/api/dynamodb/tables");
}

export async function createDynamodbTable(payload: Record<string, unknown>) {
  return fetchJSON("/api/dynamodb/tables", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function fetchDynamodbTableSummary(tableName: string): Promise<DynamoTable> {
  return fetchJSON(`/api/dynamodb/tables/${encodeURIComponent(tableName)}/summary`);
}

export async function updateDynamodbTable(tableName: string, payload: Record<string, unknown>) {
  return fetchJSON(`/api/dynamodb/tables/${encodeURIComponent(tableName)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export async function deleteDynamodbTable(tableName: string) {
  return fetchJSON(`/api/dynamodb/tables/${encodeURIComponent(tableName)}`, { method: "DELETE" });
}

export async function deleteDynamodbTableForced(tableName: string) {
  return fetchJSON(`/api/dynamodb/tables/${encodeURIComponent(tableName)}?force=true`, { method: "DELETE" });
}

export async function fetchDynamodbItems(tableName: string, limit = 25): Promise<{ items: Array<Record<string, unknown>> }> {
  return fetchJSON(`/api/dynamodb/tables/${encodeURIComponent(tableName)}/items?limit=${encodeURIComponent(String(limit))}`);
}

export async function queryDynamodbItems(tableName: string, key: Record<string, unknown>, limit = 25): Promise<{ items: Array<Record<string, unknown>> }> {
  return fetchJSON(`/api/dynamodb/tables/${encodeURIComponent(tableName)}/items/query`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ key, limit }),
  });
}

export async function putDynamodbItem(tableName: string, item: Record<string, unknown>) {
  return fetchJSON(`/api/dynamodb/tables/${encodeURIComponent(tableName)}/items`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ item }),
  });
}

export async function deleteDynamodbItem(tableName: string, key: Record<string, unknown>) {
  return fetchJSON(`/api/dynamodb/tables/${encodeURIComponent(tableName)}/items`, {
    method: "DELETE",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ key }),
  });
}


