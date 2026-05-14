import { FormEvent, useEffect, useMemo, useState, useRef } from "react";
import { Link, Navigate, Route, Routes, useNavigate, useParams } from "react-router-dom";

import {
  ApiError,
  createS3Bucket,
  createSnsTopic,
  createSqsQueue,
  deleteS3Bucket,
  deleteSnsTopic,
  deleteSnsTopicForced,
  deleteSqsQueue,
  deleteSqsQueueForced,
  fetchEndpoints,
  fetchMinistackHealth,
  fetchResources,
  fetchS3Buckets,
  fetchSnsTopicSummary,
  fetchSnsTopics,
  fetchSqsQueueSummary,
  fetchSqsQueues,
  fetchStats,
  fetchResourceDetail,
  getServiceTotal,
  type MinistackHealthResponse,
  publishSnsMessage,
  sendSqsMessage,
  subscribeSqsToSns,
  type EndpointRoute,
  type ServiceStat,
  type StatsResponse,
  updateS3Bucket,
  updateSnsTopic,
  updateSqsQueue,
  createDynamodbTable,
  deleteDynamodbItem,
  deleteDynamodbTable,
  deleteDynamodbTableForced,
  fetchDynamodbItems,
  fetchDynamodbTableSummary,
  fetchDynamodbTables,
  putDynamodbItem,
  queryDynamodbItems,
  type DynamoTable,
  updateDynamodbTable,
} from "./api";
import { useToast } from "./toast";
import { useKeyboardShortcuts } from "./hooks/useKeyboardShortcuts";
import { exportData } from "./lib/export";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from "./components/Sheet";
import { JsonViewer } from "./components/JsonViewer";
import { ResourceCrudTemplate } from "./components/ResourceCrudTemplate";

type ViewMode = "grid" | "list";
const REFRESH_OPTIONS = [
  { label: "1 min", value: 60_000 },
  { label: "5 min", value: 300_000 },
  { label: "15 min", value: 900_000 },
] as const;

const SERVICE_ICONS: Record<string, string> = {
  s3: "[S3]",
  sqs: "[SQS]",
  sns: "[SNS]",
  dynamodb: "[DDB]",
  lambda: "[LBD]",
  iam: "[IAM]",
  logs: "[LOG]",
  cloudwatch: "[CW]",
  events: "[EVT]",
  apigateway: "[API]",
};

function getServiceIcon(name: string): string {
  return SERVICE_ICONS[name] ?? "[AWS]";
}

function useFavorites() {
  const [favorites, setFavorites] = useState<string[]>(() => {
    const raw = localStorage.getItem("aws-manager:favorites");
    if (!raw) return [];
    try {
      const parsed = JSON.parse(raw) as unknown;
      return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === "string") : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    localStorage.setItem("aws-manager:favorites", JSON.stringify(favorites));
  }, [favorites]);

  function toggleFavorite(name: string) {
    setFavorites((prev) => (prev.includes(name) ? prev.filter((item) => item !== name) : [...prev, name]));
  }

  return { favorites, toggleFavorite, isFavorite: (name: string) => favorites.includes(name) };
}

function AppShell({
  children,
  health,
  refreshMs,
  setRefreshMs,
  sidebarCollapsed,
  setSidebarCollapsed,
}: {
  children: React.ReactNode;
  health: MinistackHealthResponse | null;
  refreshMs: number;
  setRefreshMs: (value: number) => void;
  sidebarCollapsed: boolean;
  setSidebarCollapsed: (value: boolean) => void;
}) {
  return (
    <div className={sidebarCollapsed ? "shell sidebar-collapsed" : "shell"}>
      <aside className="sidebar">
        <div className="brand">
          <button
            type="button"
            className="sidebar-toggle"
            onClick={() => setSidebarCollapsed(!sidebarCollapsed)}
            title={sidebarCollapsed ? "Expand menu" : "Collapse menu"}
          >
            {sidebarCollapsed ? ">" : "<"}
          </button>
          {!sidebarCollapsed && (
            <>
              <div className="brand-title">AWS Manager</div>
              <div className="brand-subtitle">StackPort-style UI</div>
            </>
          )}
        </div>
        <nav className="nav-list">
          <Link to="/" title="Dashboard">{sidebarCollapsed ? "DB" : "Dashboard"}</Link>
          <Link to="/resources" title="Resources">{sidebarCollapsed ? "RS" : "Resources"}</Link>
        </nav>
      </aside>
      <main className="content">
        <header className="topbar">
          <div className="topbar-left">MiniStack Resource Console</div>
          <div className="topbar-right">
            <span className={health?.online ? "status-chip online" : "status-chip offline"} title={health?.online ? "MiniStack online" : "MiniStack offline"}>
              <span className="status-dot-large" />
              MiniStack
            </span>
            <label className="refresh-picker">
              Check:
              <select value={String(refreshMs)} onChange={(event) => setRefreshMs(Number(event.target.value))}>
                {REFRESH_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
            <Link to="/apis" className="api-link" title="API catalog">
              APIs
            </Link>
          </div>
        </header>
        {children}
      </main>
    </div>
  );
}

function ApiCatalogPage() {
  const [routes, setRoutes] = useState<EndpointRoute[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchEndpoints()
      .then((result) => {
        setRoutes(result.routes);
        setError(null);
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : "Unknown error");
      });
  }, []);

  return (
    <div className="page">
      <div className="header-row">
        <h1>API Catalog</h1>
      </div>
      <p className="muted">Swagger-like view generated from Flask route map.</p>
      {error && <p className="error">{error}</p>}
      <div className="api-table-wrap">
        <table className="api-table">
          <thead>
            <tr>
              <th>Path</th>
              <th>Methods</th>
              <th>Endpoint</th>
            </tr>
          </thead>
          <tbody>
            {routes.map((route) => (
              <tr key={`${route.path}-${route.endpoint}`}>
                <td>{route.path}</td>
                <td>{route.methods.join(", ")}</td>
                <td>{route.endpoint}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function DashboardPage({ refreshMs, health }: { refreshMs: number; health: MinistackHealthResponse | null }) {
  const [stats, setStats] = useState<StatsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<ViewMode>("grid");
  const { favorites, toggleFavorite, isFavorite } = useFavorites();

  async function reload() {
    try {
      const next = await fetchStats();
      setStats(next);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unknown error");
    }
  }

  useEffect(() => {
    reload();
    const id = window.setInterval(reload, refreshMs);
    return () => window.clearInterval(id);
  }, [refreshMs]);

  if (error && !stats) {
    return <div className="page"><h1>Dashboard</h1><p className="error">{error}</p></div>;
  }

  const services = stats ? Object.entries(stats.services) : [];
  const favoriteServices = services.filter(([name]) => favorites.includes(name));
  const otherServices = services.filter(([name]) => !favorites.includes(name));

  function renderService([name, service]: [string, ServiceStat]) {
    const online = health?.services[name] ?? service.status === "available";
    return (
      <Link to={`/resources/${name}`} className="card" key={name}>
        <div className="card-title-row">
          <h3>
            <span className="service-icon" aria-hidden="true">{getServiceIcon(name)}</span>
            {name}
          </h3>
          <div className="card-actions">
            <button
              type="button"
              className={isFavorite(name) ? "fav-btn active" : "fav-btn"}
              onClick={(event) => {
                event.preventDefault();
                toggleFavorite(name);
              }}
              title={isFavorite(name) ? "Remove favorite" : "Add favorite"}
            >
              ★
            </button>
            <span className={online ? "status-dot on" : "status-dot off"} title={online ? "Online" : "Offline"} />
          </div>
        </div>
        <div className="card-meta">{getServiceTotal(service)} total</div>
        <div className="resource-list">
          {Object.entries(service.resources).map(([type, count]) => (
            <div key={type} className="resource-row"><span>{type}</span><span>{count}</span></div>
          ))}
        </div>
      </Link>
    );
  }

  function renderAsList(rows: Array<[string, ServiceStat]>) {
    return (
      <div className="service-table">
        {rows.map(([name, service]) => {
          const online = health?.services[name] ?? service.status === "available";
          return (
            <Link to={`/resources/${name}`} className="service-table-row" key={name}>
              <span className="service-main"><span className="service-icon">{getServiceIcon(name)}</span>{name}</span>
              <span className={online ? "status-dot on" : "status-dot off"} />
              <span>{Object.keys(service.resources).length} types</span>
              <span>{getServiceTotal(service)} total</span>
            </Link>
          );
        })}
      </div>
    );
  }

  return (
    <div className="page">
      <div className="header-row">
        <h1>Dashboard</h1>
        <div className="header-tools">
          <button onClick={() => setViewMode("grid")} className={viewMode === "grid" ? "toggle-btn active" : "toggle-btn"}>Grid</button>
          <button onClick={() => setViewMode("list")} className={viewMode === "list" ? "toggle-btn active" : "toggle-btn"}>List</button>
          <button onClick={reload}>Refresh</button>
        </div>
      </div>
      <p className="muted">{services.length} services | {stats?.total_resources ?? 0} resources</p>
      {favoriteServices.length > 0 && (
        <section>
          <h3 className="section-title">Favorites</h3>
          {viewMode === "grid" ? <div className="grid">{favoriteServices.map(renderService)}</div> : renderAsList(favoriteServices)}
        </section>
      )}
      <section>
        {favoriteServices.length > 0 && <h3 className="section-title">All services</h3>}
        {viewMode === "grid" ? <div className="grid">{otherServices.map(renderService)}</div> : renderAsList(otherServices)}
      </section>
    </div>
  );
}

function GenericBrowser({ service }: { service: string }) {
  const { showToast } = useToast();
  const [resources, setResources] = useState<Record<string, Array<Record<string, unknown>>> | null>(null);
  const [detail, setDetail] = useState<{type: string; id: string; detail: unknown} | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [pageSize, setPageSize] = useState(25);
  const [pages, setPages] = useState<Record<string, number>>({});
  const [selectedRow, setSelectedRow] = useState(-1);
  const searchInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    async function load() {
      try {
        const result = await fetchResources(service);
        setResources(result.resources);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unknown error");
      }
    }
    load();
  }, [service]);

  useEffect(() => {
    setPages({});
    setSelectedRow(-1);
  }, [search, service]);

  async function openDetail(type: string, id: string) {
    try {
      const result = await fetchResourceDetail(service, type, id);
      setDetail({ type, id, detail: result.detail });
      setError(null);
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Unknown error", "error");
    }
  }

  // Compute flat list of all visible resource items for j/k navigation
  const allVisibleItems: { type: string; id: string }[] = [];
  if (resources) {
    for (const [type, items] of Object.entries(resources)) {
      const filtered = search
        ? items.filter((item) => Object.values(item).some((value) => String(value ?? "").toLowerCase().includes(search.toLowerCase())))
        : items;
      const page = pages[type] ?? 0;
      const paginatedItems = filtered.slice(page * pageSize, (page + 1) * pageSize);
      for (const item of paginatedItems) {
        allVisibleItems.push({ type, id: String(item.id ?? "") });
      }
    }
  }

  // Keyboard shortcuts
  useKeyboardShortcuts([
    {
      key: "/",
      handler: () => searchInputRef.current?.focus(),
    },
    {
      key: "Escape",
      handler: () => {
        if (detail) {
          setDetail(null);
        } else if (selectedRow >= 0) {
          setSelectedRow(-1);
        } else {
          searchInputRef.current?.blur();
        }
      },
    },
    {
      key: "j",
      handler: () => {
        if (allVisibleItems.length === 0) return;
        setSelectedRow((prev) => Math.min(prev + 1, allVisibleItems.length - 1));
      },
    },
    {
      key: "k",
      handler: () => {
        if (allVisibleItems.length === 0) return;
        setSelectedRow((prev) => Math.max(prev - 1, 0));
      },
    },
    {
      key: "Enter",
      handler: () => {
        if (selectedRow >= 0 && selectedRow < allVisibleItems.length) {
          const item = allVisibleItems[selectedRow];
          openDetail(item.type, item.id);
        }
      },
    },
  ]);

  if (error && !resources) {
    return <p className="error">{error}</p>;
  }

  return (
    <>
      <div className="browser-layout">
        <div className="browser-list">
          <div className="list-toolbar">
            <input
              ref={searchInputRef}
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search resources (press / to focus)"
            />
            <select value={String(pageSize)} onChange={(event) => setPageSize(Number(event.target.value))}>
              <option value="25">25</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </select>
          </div>
          {resources && Object.entries(resources).map(([type, items]) => (
            <section key={type} className="list-block">
              {(() => {
                const filtered = search
                  ? items.filter((item) => Object.values(item).some((value) => String(value ?? "").toLowerCase().includes(search.toLowerCase())))
                  : items;
                const page = pages[type] ?? 0;
                const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
                const start = page * pageSize;
                const pagedItems = filtered.slice(start, start + pageSize);

                return (
                  <>
                    <div className="list-block-header">
                      <h3>{type} ({filtered.length})</h3>
                      {filtered.length > 0 && (
                        <div className="export-controls">
                          <button
                            className="export-btn"
                            onClick={() => {
                              try {
                                exportData({
                                  service,
                                  resourceType: type,
                                  data: filtered,
                                  format: "json"
                                });
                                showToast(`Exported ${type} as JSON`, "success");
                              } catch (e) {
                                showToast(e instanceof Error ? e.message : "Export failed", "error");
                              }
                            }}
                            title="Export as JSON"
                          >
                            JSON
                          </button>
                          <button
                            className="export-btn"
                            onClick={() => {
                              try {
                                exportData({
                                  service,
                                  resourceType: type,
                                  data: filtered,
                                  format: "csv"
                                });
                                showToast(`Exported ${type} as CSV`, "success");
                              } catch (e) {
                                showToast(e instanceof Error ? e.message : "Export failed", "error");
                              }
                            }}
                            title="Export as CSV"
                          >
                            CSV
                          </button>
                        </div>
                      )}
                    </div>
                    {pagedItems.length === 0 && <p className="muted">No items</p>}
                    {pagedItems.map((item, index) => {
                      const globalIdx = allVisibleItems.findIndex(
                        (x) => x.type === type && x.id === String(item.id ?? `${start + index}`)
                      );
                      const id = String(item.id ?? `${start + index}`);
                      const isSelected = globalIdx === selectedRow;
                      return (
                        <button
                          className={`list-item ${isSelected ? "selected" : ""}`}
                          key={`${type}-${id}-${start + index}`}
                          onClick={() => openDetail(type, id)}
                          data-row-index={globalIdx}
                        >
                          {id}
                        </button>
                      );
                    })}
                    {filtered.length > pageSize && (
                      <div className="pager-row">
                        <button disabled={page <= 0} onClick={() => setPages((prev) => ({ ...prev, [type]: Math.max((prev[type] ?? 0) - 1, 0) }))}>Prev</button>
                        <span>{page + 1}/{totalPages}</span>
                        <button disabled={page + 1 >= totalPages} onClick={() => setPages((prev) => ({ ...prev, [type]: Math.min((prev[type] ?? 0) + 1, totalPages - 1) }))}>Next</button>
                      </div>
                    )}
                  </>
                );
              })()}
            </section>
          ))}
        </div>
      </div>

      {/* Detail Sheet */}
      <Sheet open={!!detail} onOpenChange={(open) => !open && setDetail(null)}>
        <SheetContent>
          {detail && (
            <>
              <SheetHeader>
                <SheetTitle>{detail.type} / {detail.id}</SheetTitle>
                <SheetDescription>{service}</SheetDescription>
              </SheetHeader>
              <div className="sheet-body">
                <JsonViewer data={detail.detail} />
              </div>
            </>
          )}
        </SheetContent>
      </Sheet>
    </>
  );
}

function parseJsonObjectInput(raw: string, fieldLabel: string): Record<string, unknown> {
  try {
    const parsed = JSON.parse(raw);
    if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) {
      throw new Error(`${fieldLabel} must be a JSON object`);
    }
    return parsed as Record<string, unknown>;
  } catch {
    throw new Error(`${fieldLabel} contains invalid JSON`);
  }
}

function parseIntOrFallback(raw: string, fallback: number): number {
  const parsed = Number.parseInt(String(raw), 10);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function S3View() {
  const [buckets, setBuckets] = useState<Array<Record<string, unknown>>>([]);
  const [createTagsRaw, setCreateTagsRaw] = useState("{}");
  const [selectedBucket, setSelectedBucket] = useState("");
  const [editVersioningStatus, setEditVersioningStatus] = useState("Enabled");
  async function load() {
    const data = await fetchS3Buckets();
    setBuckets(data.buckets);
    if (!selectedBucket && data.buckets.length > 0) {
      setSelectedBucket(String(data.buckets[0].name ?? ""));
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      const tags = parseJsonObjectInput(createTagsRaw, "Create tags");
      const result = await createS3Bucket({
        bucketName: form.get("bucketName"),
        versioningStatus: form.get("versioningStatus"),
        tags,
      });
      showToast(`Created: ${(result as Record<string, unknown>).bucketName ?? "bucket"}`, "success");
      event.currentTarget.reset();
      setCreateTagsRaw("{}");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function updateSelectedBucket() {
    if (!selectedBucket) {
      showToast("Select a bucket to edit", "error");
      return;
    }
    try {
      const tags = parseJsonObjectInput(editTagsRaw, "Edit tags");
      await updateS3Bucket({ bucketName: selectedBucket, versioningStatus: editVersioningStatus, tags });
      showToast(`Updated: ${selectedBucket}`, "success");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function removeSelectedBucket() {
    if (!selectedBucket) {
      showToast("Select a bucket to delete", "error");
      return;
    }

    const confirmed = window.confirm(`Delete bucket ${selectedBucket}?`);
    if (!confirmed) return;

    try {
      await deleteS3Bucket({ bucketName: selectedBucket, forceDelete: true });
      showToast(`Deleted: ${selectedBucket}`, "success");
      setSelectedBucket("");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  const bucketOptions = buckets.map((bucket) => {
    const name = String(bucket.name ?? "");
    return <option value={name} key={name}>{name}</option>;
  });

  const sections = [
    {
      id: "create-bucket",
      title: "Create bucket",
      hint: "Choose versioning and optional tags.",
      content: (
        <form className="inline-form stacked" onSubmit={onCreate}>
          <input name="bucketName" required placeholder="bucket-name" />
          <select name="versioningStatus" defaultValue="Disabled">
            <option value="Disabled">Disabled</option>
            <option value="Enabled">Enabled</option>
            <option value="Suspended">Suspended</option>
          </select>
          <textarea
            value={createTagsRaw}
            onChange={(event) => setCreateTagsRaw(event.target.value)}
            className="json-textarea"
            placeholder='{"team":"orders"}'
          />
          <button type="submit">Create</button>
        </form>
      ),
    },
    {
      id: "edit-bucket",
      title: "Edit bucket",
      hint: "Select an existing bucket and update settings.",
      content: (
        <div className="inline-form stacked">
          <select value={selectedBucket} onChange={(event) => setSelectedBucket(event.target.value)}>
            <option value="">Select bucket</option>
            {bucketOptions}
          </select>
          <select value={editVersioningStatus} onChange={(event) => setEditVersioningStatus(event.target.value)}>
            <option value="Enabled">Enabled</option>
            <option value="Suspended">Suspended</option>
            <option value="Disabled">Disabled</option>
          </select>
          <textarea
            value={editTagsRaw}
            onChange={(event) => setEditTagsRaw(event.target.value)}
            className="json-textarea"
            placeholder='{"team":"orders","env":"dev"}'
          />
          <button type="button" onClick={updateSelectedBucket}>Save changes</button>
        </div>
      ),
    },
    {
      id: "delete-bucket",
      title: "Delete bucket",
      hint: "Delete is force-enabled for local testing flow.",
      content: (
        <div className="inline-form stacked">
          <select value={selectedBucket} onChange={(event) => setSelectedBucket(event.target.value)}>
            <option value="">Select bucket</option>
            {bucketOptions}
          </select>
          <button type="button" className="danger" onClick={removeSelectedBucket}>Delete bucket</button>
        </div>
      ),
    },
  ];

  return (
    <ResourceCrudTemplate title="S3 Bucket Management" sections={sections}>
      <div className="table">
        {buckets.map((bucket) => {
          const name = String(bucket.name ?? "");
          return (
            <div className="table-row" key={name}>
              <span>{name}</span>
              <button type="button" onClick={() => setSelectedBucket(name)}>Use in editor</button>
            </div>
          );
        })}
      </div>
    </ResourceCrudTemplate>
  );
}

function SqsView() {
  const [queues, setQueues] = useState<Array<{ name: string; url: string }>>([]);
  const [selectedQueue, setSelectedQueue] = useState("");
  const [visibilityTimeout, setVisibilityTimeout] = useState("30");
  const [messageRetentionPeriod, setMessageRetentionPeriod] = useState("345600");
  const [sendQueueName, setSendQueueName] = useState("");
  const [sendBodyRaw, setSendBodyRaw] = useState('{\n  "hello": "world"\n}');
  const { showToast } = useToast();

  async function load() {
    const data = await fetchSqsQueues();
    setQueues(data.queues);
    if (!selectedQueue && data.queues.length > 0) {
      setSelectedQueue(data.queues[0].name);
    }
    if (!sendQueueName && data.queues.length > 0) {
      setSendQueueName(data.queues[0].name);
    }
  }

  useEffect(() => { load(); }, []);

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await createSqsQueue({ queueName: form.get("queueName") });
      showToast("Queue created", "success");
      event.currentTarget.reset();
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function onSend(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const queueName = sendQueueName;
    if (!queueName) {
      showToast("Choose a queue before sending a message", "error");
      return;
    }

    try {
      const body = parseJsonObjectInput(sendBodyRaw, "Message body");
      await sendSqsMessage(queueName, { body, headers: {} });
      showToast(`Message sent to ${queueName}`, "success");
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function remove(queueName: string) {
    try {
      const summary = await fetchSqsQueueSummary(queueName);
      if (summary.hasActiveMessages) {
        const firstConfirm = window.confirm(
          `A fila ${queueName} possui mensagens ativas (${summary.visibleMessages} visiveis, ${summary.inflightMessages} inflight). Deseja continuar?`
        );
        if (!firstConfirm) return;

        const secondConfirm = window.confirm("Confirmacao final: deseja remover a fila com mensagens ativas?");
        if (!secondConfirm) return;

        await deleteSqsQueueForced(queueName);
      } else {
        await deleteSqsQueue(queueName);
      }
      showToast(`Queue deleted: ${queueName}`, "success");
      await load();
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        showToast("Fila requer confirmacao forcada para exclusao.", "error");
        return;
      }
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function startEdit(queueName: string) {
    try {
      const summary = await fetchSqsQueueSummary(queueName);
      setSelectedQueue(queueName);
      setVisibilityTimeout(String(summary.visibilityTimeout));
      setMessageRetentionPeriod(String(summary.messageRetentionPeriod));
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function saveEdit(queueName: string) {
    try {
      await updateSqsQueue(queueName, {
        visibilityTimeout: Number(visibilityTimeout),
        messageRetentionPeriod: Number(messageRetentionPeriod),
      });
      showToast(`Queue updated: ${queueName}`, "success");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  const queueOptions = queues.map((queue) => (
    <option key={queue.url} value={queue.name}>{queue.name}</option>
  ));

  const sections = [
    {
      id: "create-queue",
      title: "Create queue",
      hint: "Create a queue before sending messages.",
      content: (
        <form className="inline-form" onSubmit={onCreate}>
          <input name="queueName" required placeholder="queue-name" />
          <button type="submit">Create</button>
        </form>
      ),
    },
    {
      id: "send-message",
      title: "Send message",
      hint: "Pick a queue and send JSON payload.",
      content: (
        <form className="inline-form stacked" onSubmit={onSend}>
          <select value={sendQueueName} onChange={(event) => setSendQueueName(event.target.value)}>
            <option value="">Select queue</option>
            {queueOptions}
          </select>
          <textarea
            value={sendBodyRaw}
            onChange={(event) => setSendBodyRaw(event.target.value)}
            className="json-textarea"
            placeholder='{"hello":"world"}'
          />
          <button type="submit">Send message</button>
        </form>
      ),
    },
    {
      id: "edit-queue",
      title: "Edit queue",
      hint: "Update visibility and retention attributes.",
      content: (
        <div className="inline-form stacked">
          <select value={selectedQueue} onChange={(event) => setSelectedQueue(event.target.value)}>
            <option value="">Select queue</option>
            {queueOptions}
          </select>
          <input
            value={visibilityTimeout}
            onChange={(event) => setVisibilityTimeout(event.target.value)}
            placeholder="Visibility timeout (seconds)"
          />
          <input
            value={messageRetentionPeriod}
            onChange={(event) => setMessageRetentionPeriod(event.target.value)}
            placeholder="Retention period (seconds)"
          />
          <button type="button" onClick={() => selectedQueue && saveEdit(selectedQueue)} disabled={!selectedQueue}>Save changes</button>
        </div>
      ),
    },
    {
      id: "delete-queue",
      title: "Delete queue",
      hint: "Active messages require double confirmation.",
      content: (
        <div className="inline-form stacked">
          <select value={selectedQueue} onChange={(event) => setSelectedQueue(event.target.value)}>
            <option value="">Select queue</option>
            {queueOptions}
          </select>
          <button type="button" className="danger" onClick={() => selectedQueue && remove(selectedQueue)} disabled={!selectedQueue}>Delete queue</button>
        </div>
      ),
    },
  ];

  return (
    <ResourceCrudTemplate title="SQS Queue Management" sections={sections}>
      <div className="table">
        {queues.map((queue) => (
          <div className="table-row" key={queue.url}>
            <span>{queue.name}</span>
            <div className="table-actions">
              <button onClick={() => startEdit(queue.name)}>Use in editor</button>
              <button className="danger" onClick={() => remove(queue.name)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </ResourceCrudTemplate>
  );
}

function SnsView() {
  const [topics, setTopics] = useState<Array<{ name: string; arn: string }>>([]);
  const [queues, setQueues] = useState<Array<{ name: string; url: string }>>([]);
  const [displayName, setDisplayName] = useState("");
  const [publishTopicName, setPublishTopicName] = useState("");
  const [publishBodyRaw, setPublishBodyRaw] = useState('{\n  "event": "order-created"\n}');
  const [subscribeTopicName, setSubscribeTopicName] = useState("");
  const [subscribeQueueName, setSubscribeQueueName] = useState("");
  const [selectedTopic, setSelectedTopic] = useState("");
  const { showToast } = useToast();

  async function load() {
    const [snsData, sqsData] = await Promise.all([fetchSnsTopics(), fetchSqsQueues()]);
    setTopics(snsData.topics);
    setQueues(sqsData.queues);

    if (!publishTopicName && snsData.topics.length > 0) {
      setPublishTopicName(snsData.topics[0].name);
    }
    if (!subscribeTopicName && snsData.topics.length > 0) {
      setSubscribeTopicName(snsData.topics[0].name);
    }
    if (!subscribeQueueName && sqsData.queues.length > 0) {
      setSubscribeQueueName(sqsData.queues[0].name);
    }
    if (!selectedTopic && snsData.topics.length > 0) {
      setSelectedTopic(snsData.topics[0].name);
    }
  }

  useEffect(() => { load(); }, []);

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      await createSnsTopic({ topicName: form.get("topicName") });
      showToast("Topic created", "success");
      event.currentTarget.reset();
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function onPublish(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const topicName = publishTopicName;
    if (!topicName) {
      showToast("Choose a topic before publishing", "error");
      return;
    }

    try {
      const body = parseJsonObjectInput(publishBodyRaw, "Publish body");
      await publishSnsMessage(topicName, { body, headers: {} });
      showToast(`Published to ${topicName}`, "success");
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function onSubscribe(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      const topicName = subscribeTopicName;
      const queueName = subscribeQueueName;
      if (!topicName || !queueName) {
        showToast("Choose topic and queue before subscribing", "error");
        return;
      }
      await subscribeSqsToSns({ topicName, queueName });
      showToast(`Subscribed ${queueName} to ${topicName}`, "success");
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function remove(topicName: string) {
    try {
      const summary = await fetchSnsTopicSummary(topicName);
      if (summary.hasActiveSubscriptions) {
        const firstConfirm = window.confirm(
          `O topico ${topicName} possui ${summary.subscriptionsCount} subscription(s) ativa(s). Deseja continuar?`
        );
        if (!firstConfirm) return;

        const secondConfirm = window.confirm("Confirmacao final: deseja remover o topico com subscriptions ativas?");
        if (!secondConfirm) return;

        await deleteSnsTopicForced(topicName);
      } else {
        await deleteSnsTopic(topicName);
      }
      showToast(`Topic deleted: ${topicName}`, "success");
      await load();
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        showToast("Topico requer confirmacao forcada para exclusao.", "error");
        return;
      }
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function startEdit(topicName: string) {
    try {
      const summary = await fetchSnsTopicSummary(topicName);
      setSelectedTopic(topicName);
      setDisplayName(summary.displayName || "");
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function saveEdit(topicName: string) {
    try {
      await updateSnsTopic(topicName, { displayName });
      showToast(`Topic updated: ${topicName}`, "success");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  const topicOptions = topics.map((topic) => (
    <option key={topic.arn} value={topic.name}>{topic.name}</option>
  ));
  const queueOptions = queues.map((queue) => (
    <option key={queue.url} value={queue.name}>{queue.name}</option>
  ));

  const sections = [
    {
      id: "create-topic",
      title: "Create topic",
      hint: "Create topic before publishing or subscribing.",
      content: (
        <form className="inline-form" onSubmit={onCreate}>
          <input name="topicName" required placeholder="topic-name" />
          <button type="submit">Create topic</button>
        </form>
      ),
    },
    {
      id: "publish-message",
      title: "Publish message",
      hint: "Pick a topic and send JSON payload.",
      content: (
        <form className="inline-form stacked" onSubmit={onPublish}>
          <select value={publishTopicName} onChange={(event) => setPublishTopicName(event.target.value)}>
            <option value="">Select topic</option>
            {topicOptions}
          </select>
          <textarea
            value={publishBodyRaw}
            onChange={(event) => setPublishBodyRaw(event.target.value)}
            className="json-textarea"
            placeholder='{"event":"order-created"}'
          />
          <button type="submit">Publish</button>
        </form>
      ),
    },
    {
      id: "subscribe-queue",
      title: "Subscribe queue",
      hint: "Connect an SQS queue to a topic.",
      content: (
        <form className="inline-form" onSubmit={onSubscribe}>
          <select value={subscribeTopicName} onChange={(event) => setSubscribeTopicName(event.target.value)}>
            <option value="">Select topic</option>
            {topicOptions}
          </select>
          <select value={subscribeQueueName} onChange={(event) => setSubscribeQueueName(event.target.value)}>
            <option value="">Select queue</option>
            {queueOptions}
          </select>
          <button type="submit">Subscribe SQS</button>
        </form>
      ),
    },
    {
      id: "edit-topic",
      title: "Edit topic",
      hint: "Display name can be updated at any time.",
      content: (
        <div className="inline-form stacked">
          <select value={selectedTopic} onChange={(event) => setSelectedTopic(event.target.value)}>
            <option value="">Select topic</option>
            {topicOptions}
          </select>
          <input
            value={displayName}
            onChange={(event) => setDisplayName(event.target.value)}
            placeholder="Display name"
          />
          <button type="button" onClick={() => selectedTopic && saveEdit(selectedTopic)} disabled={!selectedTopic}>Save changes</button>
        </div>
      ),
    },
    {
      id: "delete-topic",
      title: "Delete topic",
      hint: "Active subscriptions require double confirmation.",
      content: (
        <div className="inline-form stacked">
          <select value={selectedTopic} onChange={(event) => setSelectedTopic(event.target.value)}>
            <option value="">Select topic</option>
            {topicOptions}
          </select>
          <button type="button" className="danger" onClick={() => selectedTopic && remove(selectedTopic)} disabled={!selectedTopic}>Delete topic</button>
        </div>
      ),
    },
  ];

  return (
    <ResourceCrudTemplate title="SNS Topic Management" sections={sections}>
      <div className="table">
        {topics.map((topic) => (
          <div className="table-row" key={topic.arn}>
            <span>{topic.name}</span>
            <div className="table-actions">
              <button onClick={() => startEdit(topic.name)}>Use in editor</button>
              <button className="danger" onClick={() => remove(topic.name)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </ResourceCrudTemplate>
  );
}

function DynamodbView() {
  const [tables, setTables] = useState<DynamoTable[]>([]);
  const [tableSearch, setTableSearch] = useState("");
  const [selectedTable, setSelectedTable] = useState("");
  const [selectedHashKey, setSelectedHashKey] = useState("id");
  const [editingTableName, setEditingTableName] = useState("");
  const [autoPreview, setAutoPreview] = useState(true);

  const [createTableName, setCreateTableName] = useState("");
  const [createHashKey, setCreateHashKey] = useState("id");
  const [createBillingMode, setCreateBillingMode] = useState("PAY_PER_REQUEST");
  const [createReadCapacity, setCreateReadCapacity] = useState("5");
  const [createWriteCapacity, setCreateWriteCapacity] = useState("5");
  const [createTableSheetOpen, setCreateTableSheetOpen] = useState(false);

  const [editBillingMode, setEditBillingMode] = useState("PAY_PER_REQUEST");
  const [editReadCapacity, setEditReadCapacity] = useState("5");
  const [editWriteCapacity, setEditWriteCapacity] = useState("5");

  const [queryMode, setQueryMode] = useState<"scan" | "query">("scan");
  const [itemsLimit, setItemsLimit] = useState("25");
  const [itemsFilter, setItemsFilter] = useState("");
  const [queryKeyRaw, setQueryKeyRaw] = useState('{\n  "id": ""\n}');
  const [itemsPreview, setItemsPreview] = useState<Array<Record<string, unknown>>>([]);
  const [runBanner, setRunBanner] = useState<{ type: "success" | "error"; message: string } | null>(null);
  const [showFilters, setShowFilters] = useState(false);

  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
  const [bulkAction, setBulkAction] = useState<"copy" | "delete">("copy");
  const [itemSheetOpen, setItemSheetOpen] = useState(false);
  const [itemSheetMode, setItemSheetMode] = useState<"create" | "edit">("create");
  const [itemSheetRaw, setItemSheetRaw] = useState('{\n  "id": "1",\n  "name": "example"\n}');
  const [itemSheetKeyRaw, setItemSheetKeyRaw] = useState('{\n  "id": "1"\n}');

  const { showToast } = useToast();

  async function loadTables() {
    const data = await fetchDynamodbTables();
    setTables(data.tables);

    if (!selectedTable && data.tables.length > 0) {
      const first = data.tables[0].name;
      setSelectedTable(first);
      await loadTableSummary(first);
      return;
    }

    const stillExists = data.tables.some((table) => table.name === selectedTable);
    if (!stillExists && data.tables.length > 0) {
      const first = data.tables[0].name;
      setSelectedTable(first);
      await loadTableSummary(first);
    }
  }

  async function loadTableSummary(tableName: string) {
    const summary = await fetchDynamodbTableSummary(tableName);
    const hashKey = summary.hashKey || "id";
    setSelectedHashKey(hashKey);
    setEditBillingMode(summary.billingMode || "PAY_PER_REQUEST");
    setEditReadCapacity(String(summary.readCapacity || 5));
    setEditWriteCapacity(String(summary.writeCapacity || 5));
    setQueryKeyRaw(JSON.stringify({ [hashKey]: "" }, null, 2));
    return summary;
  }

  useEffect(() => {
    loadTables().catch((err) => showToast(err instanceof Error ? err.message : "Error", "error"));
  }, []);

  function parseLimit() {
    return parseIntOrFallback(itemsLimit, 25);
  }

  async function runScan(tableName: string) {
    const data = await fetchDynamodbItems(tableName, parseLimit());
    setItemsPreview(data.items || []);
    setSelectedRowKeys([]);
    setRunBanner({ type: "success", message: `Completed. Loaded ${data.items.length} item(s).` });
  }

  async function runQuery(tableName: string) {
    const key = parseJsonObjectInput(queryKeyRaw, "Query key");
    const data = await queryDynamodbItems(tableName, key, parseLimit());
    setItemsPreview(data.items || []);
    setSelectedRowKeys([]);
    setRunBanner({ type: "success", message: `Completed. Query returned ${data.items.length} item(s).` });
  }

  async function runSearch(tableName: string) {
    if (queryMode === "query") {
      await runQuery(tableName);
      return;
    }
    await runScan(tableName);
  }

  async function onSelectTable(tableName: string, options?: { runPreview?: boolean }) {
    if (!tableName) return;
    setSelectedTable(tableName);
    setEditingTableName("");
    setItemsFilter("");
    try {
      await loadTableSummary(tableName);
      if (options?.runPreview) {
        await runScan(tableName);
      }
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function onRun() {
    if (!selectedTable) {
      showToast("Select a table before running", "error");
      return;
    }
    try {
      await runSearch(selectedTable);
    } catch (err) {
      const message = err instanceof Error ? err.message : "Error";
      setRunBanner({ type: "error", message });
      showToast(message, "error");
    }
  }

  function onResetSearch() {
    setQueryMode("scan");
    setItemsFilter("");
    setRunBanner(null);
    setQueryKeyRaw(JSON.stringify({ [selectedHashKey || "id"]: "" }, null, 2));
  }

  function getRowKey(item: Record<string, unknown>, index: number): string {
    const byHash = selectedHashKey ? item[selectedHashKey] : undefined;
    if (byHash !== undefined && byHash !== null && String(byHash).trim() !== "") {
      return String(byHash);
    }
    return `${index}-${JSON.stringify(item)}`;
  }

  function buildItemKey(item: Record<string, unknown>) {
    if (!selectedHashKey) return {};
    return { [selectedHashKey]: item[selectedHashKey] ?? "" };
  }

  async function refreshAfterMutation() {
    if (!selectedTable) return;
    await loadTables();
    await loadTableSummary(selectedTable);
    await runScan(selectedTable);
  }

  async function onCreateTable(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!createTableName.trim() || !createHashKey.trim()) {
      showToast("tableName and hashKey are required", "error");
      return;
    }

    try {
      await createDynamodbTable({
        tableName: createTableName.trim(),
        hashKey: createHashKey.trim(),
        billingMode: createBillingMode,
        readCapacity: Number(createReadCapacity || "5"),
        writeCapacity: Number(createWriteCapacity || "5"),
      });
      showToast(`Table created: ${createTableName.trim()}`, "success");
      const tableName = createTableName.trim();
      setCreateTableName("");
      setCreateHashKey("id");
      setCreateBillingMode("PAY_PER_REQUEST");
      setCreateReadCapacity("5");
      setCreateWriteCapacity("5");
      setCreateTableSheetOpen(false);
      await loadTables();
      await onSelectTable(tableName, { runPreview: true });
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function onSaveTableSettings() {
    if (!selectedTable) {
      showToast("Select a table to edit", "error");
      return;
    }

    try {
      await updateDynamodbTable(selectedTable, {
        billingMode: editBillingMode,
        readCapacity: Number(editReadCapacity || "5"),
        writeCapacity: Number(editWriteCapacity || "5"),
      });
      showToast(`Table updated: ${selectedTable}`, "success");
      setEditingTableName("");
      await loadTables();
      await loadTableSummary(selectedTable);
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function onDeleteTable(tableName: string) {
    if (!tableName) return;
    try {
      const summary = await fetchDynamodbTableSummary(tableName);
      if (summary.itemCount > 0) {
        const firstConfirm = window.confirm(`A tabela ${tableName} possui ${summary.itemCount} item(ns). Deseja continuar?`);
        if (!firstConfirm) return;

        const secondConfirm = window.confirm("Confirmacao final: deseja remover a tabela com itens?");
        if (!secondConfirm) return;
        await deleteDynamodbTableForced(tableName);
      } else {
        await deleteDynamodbTable(tableName);
      }

      showToast(`Table deleted: ${tableName}`, "success");
      if (selectedTable === tableName) {
        setSelectedTable("");
        setSelectedHashKey("id");
        setItemsPreview([]);
        setSelectedRowKeys([]);
      }
      if (editingTableName === tableName) {
        setEditingTableName("");
      }
      await loadTables();
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        showToast("Table requires forced delete confirmation.", "error");
        return;
      }
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  function openCreateItemDrawer() {
    if (!selectedTable) {
      showToast("Select a table before creating an item", "error");
      return;
    }
    const hashKey = selectedHashKey || "id";
    const next = JSON.stringify({ [hashKey]: "", name: "example" }, null, 2);
    setItemSheetMode("create");
    setItemSheetRaw(next);
    setItemSheetKeyRaw(JSON.stringify({ [hashKey]: "" }, null, 2));
    setItemSheetOpen(true);
  }

  function openEditItemDrawer(item: Record<string, unknown>) {
    setItemSheetMode("edit");
    setItemSheetRaw(JSON.stringify(item, null, 2));
    setItemSheetKeyRaw(JSON.stringify(buildItemKey(item), null, 2));
    setItemSheetOpen(true);
  }

  async function onSaveItemFromDrawer() {
    if (!selectedTable) {
      showToast("Select a table before saving item", "error");
      return;
    }
    try {
      const item = parseJsonObjectInput(itemSheetRaw, "Item payload");
      await putDynamodbItem(selectedTable, item);
      setItemSheetOpen(false);
      showToast(itemSheetMode === "create" ? "Item created" : "Item updated", "success");
      await refreshAfterMutation();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function deleteItemByKey(key: Record<string, unknown>, displayText: string, askConfirmation = true) {
    if (!selectedTable) return;
    if (askConfirmation) {
      const confirmed = window.confirm(`Delete item ${displayText} from ${selectedTable}?`);
      if (!confirmed) return;
    }
    await deleteDynamodbItem(selectedTable, key);
  }

  async function onDeletePreviewItem(item: Record<string, unknown>) {
    if (!selectedTable) return;
    try {
      const key = buildItemKey(item);
      const hashValue = selectedHashKey ? String(key[selectedHashKey] ?? "?") : "?";
      await deleteItemByKey(key, `${selectedHashKey}=${hashValue}`);
      showToast("Item deleted", "success");
      await refreshAfterMutation();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function onBulkAction(action: string) {
    if (selectedRowKeys.length === 0) {
      showToast("Select at least one item", "error");
      return;
    }

    const selectedItems = filteredItemsPreview.filter((item, index) => selectedRowKeys.includes(getRowKey(item, index)));

    if (action === "copy") {
      try {
        await navigator.clipboard.writeText(JSON.stringify(selectedItems, null, 2));
        showToast("Selected items copied", "success");
      } catch {
        showToast("Clipboard unavailable", "error");
      }
      return;
    }

    if (action === "delete") {
      const confirmed = window.confirm(`Delete ${selectedItems.length} selected item(s)?`);
      if (!confirmed) return;
      try {
        for (const item of selectedItems) {
          await deleteItemByKey(buildItemKey(item), "selected item", false);
        }
        setSelectedRowKeys([]);
        showToast("Selected items deleted", "success");
        await refreshAfterMutation();
      } catch (err) {
        showToast(err instanceof Error ? err.message : "Error", "error");
      }
    }
  }

  async function onApplyBulkAction() {
    await onBulkAction(bulkAction);
  }

  const filteredTables = useMemo(() => {
    const term = tableSearch.trim().toLowerCase();
    if (!term) return tables;
    return tables.filter((table) => table.name.toLowerCase().includes(term));
  }, [tableSearch, tables]);

  const filteredItemsPreview = useMemo(() => {
    const term = itemsFilter.trim().toLowerCase();
    if (!term) return itemsPreview;
    return itemsPreview.filter((item) => String(item[selectedHashKey] ?? "").toLowerCase().includes(term));
  }, [itemsFilter, itemsPreview, selectedHashKey]);

  const itemColumns = useMemo(() => {
    const keys = new Set<string>();
    for (const item of filteredItemsPreview) {
      Object.keys(item).forEach((key) => keys.add(key));
    }
    return Array.from(keys);
  }, [filteredItemsPreview]);

  const visibleRowKeys = useMemo(
    () => filteredItemsPreview.map((item, index) => getRowKey(item, index)),
    [filteredItemsPreview, selectedHashKey]
  );
  const allVisibleSelected = visibleRowKeys.length > 0 && visibleRowKeys.every((key) => selectedRowKeys.includes(key));

  useEffect(() => {
    if (!autoPreview || !selectedTable) return;
    runScan(selectedTable).catch(() => {
      // keep toast handling in explicit actions
    });
  }, [autoPreview, selectedTable]);

  return (
    <div className="ddb-console">
      <aside className="ddb-console-sidebar">
        <div className="ddb-sidebar-header-row">
          <h3>Tables ({tables.length})</h3>
          <button type="button" onClick={() => setCreateTableSheetOpen(true)}>Create table</button>
        </div>
        <input
          value={tableSearch}
          onChange={(event) => setTableSearch(event.target.value)}
          placeholder="Find tables by table name"
        />
        <div className="ddb-sidebar-list">
          {filteredTables.map((table) => (
            <div className={selectedTable === table.name ? "ddb-sidebar-item selected" : "ddb-sidebar-item"} key={table.name}>
              <button type="button" className="ddb-sidebar-item-main" onClick={() => onSelectTable(table.name, { runPreview: autoPreview })}>
                <span>{table.name}</span>
                <small>{table.itemCount} item(s)</small>
              </button>
              <div className="ddb-sidebar-item-actions">
                <button type="button" onClick={() => onSelectTable(table.name, { runPreview: true })}>Items</button>
                <button type="button" onClick={() => { setEditingTableName(table.name); onSelectTable(table.name); }}>Edit</button>
                <button type="button" className="danger" onClick={() => onDeleteTable(table.name)}>Delete</button>
              </div>
              {editingTableName === table.name && (
                <div className="ddb-inline-table-editor">
                  <select value={editBillingMode} onChange={(event) => setEditBillingMode(event.target.value)}>
                    <option value="PAY_PER_REQUEST">PAY_PER_REQUEST</option>
                    <option value="PROVISIONED">PROVISIONED</option>
                  </select>
                  {editBillingMode === "PROVISIONED" && (
                    <>
                      <input value={editReadCapacity} onChange={(event) => setEditReadCapacity(event.target.value)} placeholder="Read capacity" />
                      <input value={editWriteCapacity} onChange={(event) => setEditWriteCapacity(event.target.value)} placeholder="Write capacity" />
                    </>
                  )}
                  <div className="table-actions">
                    <button type="button" onClick={onSaveTableSettings}>Save</button>
                    <button type="button" onClick={() => setEditingTableName("")}>Cancel</button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </aside>

      <section className="ddb-console-main">
        <div className="ddb-breadcrumb">
          <span>DynamoDB</span>
          <span>&gt;</span>
          <span>Explore items</span>
          {selectedTable && (
            <>
              <span>&gt;</span>
              <span>{selectedTable}</span>
            </>
          )}
        </div>
        <div className="ddb-console-header">
          <h2>{selectedTable || "Select a table"}</h2>
          <div className="ddb-console-header-actions">
            <label className="inline-check">
              <input type="checkbox" checked={autoPreview} onChange={(event) => setAutoPreview(event.target.checked)} />
              Autopreview
            </label>
            <button type="button" disabled={!selectedTable}>View table details</button>
          </div>
        </div>

        <section className="ddb-panel">
          <h3>Scan or query items</h3>
          <div className="ddb-mode-toggle">
            <button type="button" className={queryMode === "scan" ? "active" : ""} onClick={() => setQueryMode("scan")}>Scan</button>
            <button type="button" className={queryMode === "query" ? "active" : ""} onClick={() => setQueryMode("query")}>Query</button>
          </div>

          <div className="ddb-controls-grid">
            <div className="inline-form stacked">
              <label>Select a table or index</label>
              <select value={selectedTable} onChange={(event) => onSelectTable(event.target.value, { runPreview: autoPreview })}>
                <option value="">Select table</option>
                {tables.map((table) => (
                  <option key={table.name} value={table.name}>{table.name}</option>
                ))}
              </select>
            </div>
            <div className="inline-form stacked">
              <label>Select attribute projection</label>
              <select defaultValue="all">
                <option value="all">All attributes</option>
              </select>
            </div>
          </div>

          <button type="button" className="ddb-filters-toggle" onClick={() => setShowFilters((prev) => !prev)}>
            {showFilters ? "Hide filters" : "Filters"}
          </button>
          {showFilters && (
            <div className="inline-form stacked">
              <label>Filter returned items by {selectedHashKey || "hash key"}</label>
              <input
                value={itemsFilter}
                onChange={(event) => setItemsFilter(event.target.value)}
                placeholder={`e.g. ${selectedHashKey || "id"}`}
                disabled={!selectedTable}
              />
            </div>
          )}

          {queryMode === "query" && (
            <div className="inline-form stacked">
              <label>Query key JSON</label>
              <textarea
                value={queryKeyRaw}
                onChange={(event) => setQueryKeyRaw(event.target.value)}
                className="json-textarea"
                placeholder='{"id":"123"}'
              />
            </div>
          )}

          <div className="inline-form">
            <input value={itemsLimit} onChange={(event) => setItemsLimit(event.target.value)} placeholder="Limit (1-100)" className="small-input" />
            <button type="button" onClick={onRun} disabled={!selectedTable}>Run</button>
            <button type="button" onClick={onResetSearch}>Reset</button>
          </div>
        </section>

        {runBanner && (
          <div className={runBanner.type === "success" ? "ddb-status-banner success" : "ddb-status-banner error"}>
            {runBanner.message}
          </div>
        )}

        <section className="ddb-panel">
          <div className="ddb-results-head">
            <h3>Items returned ({filteredItemsPreview.length})</h3>
            <div className="table-actions">
              <button type="button" onClick={() => selectedTable && runScan(selectedTable)} disabled={!selectedTable}>Refresh</button>
              <select value={bulkAction} onChange={(event) => setBulkAction(event.target.value as "copy" | "delete")}>
                <option value="copy">Actions: Copy selected</option>
                <option value="delete">Actions: Delete selected</option>
              </select>
              <button type="button" onClick={onApplyBulkAction} disabled={selectedRowKeys.length === 0}>Apply</button>
              <button type="button" onClick={openCreateItemDrawer} disabled={!selectedTable}>Create item</button>
            </div>
          </div>

          {filteredItemsPreview.length === 0 ? (
            <p className="muted">No items found.</p>
          ) : (
            <div className="ddb-results-table-wrap">
              <table className="ddb-results-table">
                <thead>
                  <tr>
                    <th></th>
                    {itemColumns.map((column) => (
                      <th key={column}>{column}</th>
                    ))}
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredItemsPreview.map((item, index) => {
                    const rowKey = getRowKey(item, index);
                    const isSelected = selectedRowKeys.includes(rowKey);
                    return (
                      <tr key={rowKey}>
                        <td>
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={(event) => {
                              const checked = event.target.checked;
                              setSelectedRowKeys((prev) => {
                                if (checked) return [...prev, rowKey];
                                return prev.filter((value) => value !== rowKey);
                              });
                            }}
                          />
                        </td>
                        {itemColumns.map((column) => (
                          <td key={`${rowKey}-${column}`}>{String(item[column] ?? "")}</td>
                        ))}
                        <td>
                          <div className="table-actions">
                            <button type="button" onClick={() => openEditItemDrawer(item)}>Edit</button>
                            <button type="button" className="danger" onClick={() => onDeletePreviewItem(item)}>Delete</button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
                <tfoot>
                  <tr>
                    <td>
                      <input
                        type="checkbox"
                        checked={allVisibleSelected}
                        onChange={(event) => {
                          const checked = event.target.checked;
                          setSelectedRowKeys((prev) => {
                            if (checked) {
                              return Array.from(new Set([...prev, ...visibleRowKeys]));
                            }
                            return prev.filter((key) => !visibleRowKeys.includes(key));
                          });
                        }}
                      />
                    </td>
                    <td colSpan={itemColumns.length + 1}>
                      Select all visible ({visibleRowKeys.length})
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          )}
        </section>
      </section>

      <Sheet open={itemSheetOpen} onOpenChange={setItemSheetOpen}>
        <SheetContent>
          <SheetHeader>
            <SheetTitle>{itemSheetMode === "create" ? "Create item" : "Edit item"}</SheetTitle>
            <SheetDescription>{selectedTable || "DynamoDB"}</SheetDescription>
          </SheetHeader>
          <div className="sheet-body">
            <div className="inline-form stacked">
              <label>Item payload JSON</label>
              <textarea
                value={itemSheetRaw}
                onChange={(event) => setItemSheetRaw(event.target.value)}
                className="json-textarea"
                placeholder='{"id":"1","name":"example"}'
              />
              <label>Item key JSON</label>
              <textarea
                value={itemSheetKeyRaw}
                onChange={(event) => setItemSheetKeyRaw(event.target.value)}
                className="json-textarea"
                placeholder='{"id":"1"}'
              />
              <div className="table-actions">
                <button type="button" onClick={onSaveItemFromDrawer}>Save item</button>
                <button type="button" onClick={() => setItemSheetOpen(false)}>Cancel</button>
              </div>
            </div>
          </div>
        </SheetContent>
      </Sheet>

      <Sheet open={createTableSheetOpen} onOpenChange={setCreateTableSheetOpen}>
        <SheetContent>
          <SheetHeader>
            <SheetTitle>Create table</SheetTitle>
            <SheetDescription>DynamoDB</SheetDescription>
          </SheetHeader>
          <div className="sheet-body">
            <form className="inline-form stacked" onSubmit={onCreateTable}>
              <input value={createTableName} onChange={(event) => setCreateTableName(event.target.value)} placeholder="table-name" required />
              <input value={createHashKey} onChange={(event) => setCreateHashKey(event.target.value)} placeholder="hash key (partition key)" required />
              <select value={createBillingMode} onChange={(event) => setCreateBillingMode(event.target.value)}>
                <option value="PAY_PER_REQUEST">PAY_PER_REQUEST</option>
                <option value="PROVISIONED">PROVISIONED</option>
              </select>
              {createBillingMode === "PROVISIONED" && (
                <>
                  <input value={createReadCapacity} onChange={(event) => setCreateReadCapacity(event.target.value)} placeholder="Read capacity" />
                  <input value={createWriteCapacity} onChange={(event) => setCreateWriteCapacity(event.target.value)} placeholder="Write capacity" />
                </>
              )}
              <div className="table-actions">
                <button type="submit">Create</button>
                <button type="button" onClick={() => setCreateTableSheetOpen(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </SheetContent>
      </Sheet>
    </div>
  );
}

function ResourcePage() {
  const params = useParams<{ service?: string }>();
  const navigate = useNavigate();
  const [stats, setStats] = useState<Record<string, ServiceStat>>({});
  const [serviceSearch, setServiceSearch] = useState("");
  const { favorites, toggleFavorite } = useFavorites();

  useEffect(() => {
    fetchStats().then((result) => setStats(result.services)).catch(() => setStats({}));
  }, []);

  const service = params.service;
  const serviceNames = useMemo(() => Object.keys(stats).sort(), [stats]);
  const filteredNames = serviceNames.filter((name) => name.includes(serviceSearch.toLowerCase()));
  const orderedNames = [...filteredNames.filter((name) => favorites.includes(name)), ...filteredNames.filter((name) => !favorites.includes(name))];

  let content = <div className="muted">Select a service.</div>;
  if (service === "s3") content = <S3View />;
  else if (service === "sqs") content = <SqsView />;
  else if (service === "sns") content = <SnsView />;
  else if (service === "dynamodb") content = <DynamodbView />;
  else if (service) content = <GenericBrowser service={service} />;

  return (
    <div className="page">
      <div className="header-row">
        <h1>Resources</h1>
      </div>
      <div className="browser-layout">
        <aside className="service-picker">
          <input
            value={serviceSearch}
            onChange={(event) => setServiceSearch(event.target.value)}
            placeholder="Find service"
            className="service-search"
          />
          {orderedNames.map((name) => (
            <div className="service-picker-row" key={name}>
              <button className={name === service ? "active" : ""} onClick={() => navigate(`/resources/${name}`)}>
                <span className="service-icon">{getServiceIcon(name)}</span>
                {name}
              </button>
              <button className={favorites.includes(name) ? "fav-btn active" : "fav-btn"} onClick={() => toggleFavorite(name)} title="Toggle favorite">★</button>
            </div>
          ))}
        </aside>
        <section className="browser-detail">{content}</section>
      </div>
    </div>
  );
}

export default function App() {
  const [refreshMs, setRefreshMs] = useState<number>(() => {
    const raw = localStorage.getItem("aws-manager:refresh-ms");
    const numeric = Number(raw);
    return REFRESH_OPTIONS.some((item) => item.value === numeric) ? numeric : REFRESH_OPTIONS[1].value;
  });
  const [health, setHealth] = useState<MinistackHealthResponse | null>(null);
  const [sidebarCollapsed, setSidebarCollapsed] = useState<boolean>(() => localStorage.getItem("aws-manager:sidebar-collapsed") === "true");

  useEffect(() => {
    localStorage.setItem("aws-manager:refresh-ms", String(refreshMs));
  }, [refreshMs]);

  useEffect(() => {
    localStorage.setItem("aws-manager:sidebar-collapsed", String(sidebarCollapsed));
  }, [sidebarCollapsed]);

  useEffect(() => {
    let active = true;
    async function checkHealth() {
      try {
        const result = await fetchMinistackHealth();
        if (active) {
          setHealth(result);
        }
      } catch {
        if (active) {
          setHealth({
            online: false,
            services: {},
            checked_at: new Date().toISOString(),
          });
        }
      }
    }

    checkHealth();
    const id = window.setInterval(checkHealth, refreshMs);
    return () => {
      active = false;
      window.clearInterval(id);
    };
  }, [refreshMs]);

  return (
    <AppShell
      health={health}
      refreshMs={refreshMs}
      setRefreshMs={setRefreshMs}
      sidebarCollapsed={sidebarCollapsed}
      setSidebarCollapsed={setSidebarCollapsed}
    >
      <Routes>
        <Route path="/" element={<DashboardPage refreshMs={refreshMs} health={health} />} />
        <Route path="/apis" element={<ApiCatalogPage />} />
        <Route path="/resources" element={<ResourcePage />} />
        <Route path="/resources/:service" element={<ResourcePage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppShell>
  );
}


