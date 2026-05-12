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

async function fetchJSON<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, init);
  if (!response.ok) {
    let detail = `${response.status}: ${response.statusText}`;
    try {
      const body = (await response.json()) as { error?: string };
      if (body?.error) {
        detail = `${response.status}: ${body.error}`;
      }
    } catch {
      // Keep fallback message when response is not JSON.
    }
    throw new Error(detail);
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

export async function publishSnsMessage(name: string, payload: Record<string, unknown>) {
  return fetchJSON(`/api/sns/topics/${encodeURIComponent(name)}/publish`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}

export async function subscribeSqsToSns(payload: Record<string, unknown>) {
  return fetchJSON("/api/sns/subscriptions/sqs", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
}


