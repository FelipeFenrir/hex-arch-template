import { FormEvent, useEffect, useMemo, useState, useRef } from "react";
import { Link, Navigate, Route, Routes, useNavigate, useParams } from "react-router-dom";

import {
  createS3Bucket,
  createSnsTopic,
  createSqsQueue,
  deleteS3Bucket,
  deleteSnsTopic,
  deleteSqsQueue,
  fetchEndpoints,
  fetchMinistackHealth,
  fetchResources,
  fetchS3Buckets,
  fetchSnsTopics,
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
} from "./api";
import { useToast } from "./toast";
import { useKeyboardShortcuts } from "./hooks/useKeyboardShortcuts";
import { exportData } from "./lib/export";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from "./components/Sheet";
import { JsonViewer } from "./components/JsonViewer";

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
}: {
  children: React.ReactNode;
  health: MinistackHealthResponse | null;
  refreshMs: number;
  setRefreshMs: (value: number) => void;
}) {
  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-title">AWS Manager</div>
          <div className="brand-subtitle">StackPort-style UI</div>
        </div>
        <nav className="nav-list">
          <Link to="/">Dashboard</Link>
          <Link to="/resources">Resources</Link>
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

function S3View() {
  const [buckets, setBuckets] = useState<Array<Record<string, unknown>>>([]);
  const { showToast } = useToast();

  async function load() {
    const data = await fetchS3Buckets();
    setBuckets(data.buckets);
  }

  useEffect(() => {
    load();
  }, []);

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      const result = await createS3Bucket({
        bucketName: form.get("bucketName"),
        versioningStatus: form.get("versioningStatus"),
        tags: {},
      });
      showToast(`Created: ${(result as Record<string, unknown>).bucketName ?? "bucket"}`, "success");
      event.currentTarget.reset();
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function updateVersioning(bucketName: string, versioningStatus: string) {
    try {
      await updateS3Bucket({ bucketName, versioningStatus, tags: {} });
      showToast(`Updated: ${bucketName}`, "success");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function remove(bucketName: string) {
    try {
      await deleteS3Bucket({ bucketName, forceDelete: true });
      showToast(`Deleted: ${bucketName}`, "success");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  return (
    <div className="service-view">
      <h2>S3 Bucket Management</h2>
      <form className="inline-form" onSubmit={onCreate}>
        <input name="bucketName" required placeholder="bucket-name" />
        <select name="versioningStatus" defaultValue="Disabled">
          <option value="Disabled">Disabled</option>
          <option value="Enabled">Enabled</option>
          <option value="Suspended">Suspended</option>
        </select>
        <button type="submit">Create</button>
      </form>
      <div className="table">
        {buckets.map((bucket) => {
          const name = String(bucket.name ?? "");
          return (
            <div className="table-row" key={name}>
              <span>{name}</span>
              <div className="table-actions">
                <button onClick={() => updateVersioning(name, "Enabled")}>Enable versioning</button>
                <button onClick={() => updateVersioning(name, "Suspended")}>Suspend versioning</button>
                <button onClick={() => remove(name)} className="danger">Delete</button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function SqsView() {
  const [queues, setQueues] = useState<Array<{ name: string; url: string }>>([]);
  const { showToast } = useToast();

  async function load() {
    const data = await fetchSqsQueues();
    setQueues(data.queues);
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
    const form = new FormData(event.currentTarget);
    const queueName = String(form.get("queueName") || "");
    const bodyRaw = String(form.get("body") || "{}");
    try {
      await sendSqsMessage(queueName, { body: JSON.parse(bodyRaw), headers: {} });
      showToast(`Message sent to ${queueName}`, "success");
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function remove(queueName: string) {
    try {
      await deleteSqsQueue(queueName);
      showToast(`Queue deleted: ${queueName}`, "success");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  return (
    <div className="service-view">
      <h2>SQS Queue Management</h2>
      <form className="inline-form" onSubmit={onCreate}>
        <input name="queueName" required placeholder="queue-name" />
        <button type="submit">Create</button>
      </form>
      <form className="inline-form" onSubmit={onSend}>
        <input name="queueName" required placeholder="target queue" />
        <input name="body" defaultValue='{"hello":"world"}' />
        <button type="submit">Send Message</button>
      </form>
      <div className="table">
        {queues.map((queue) => (
          <div className="table-row" key={queue.url}>
            <span>{queue.name}</span>
            <button className="danger" onClick={() => remove(queue.name)}>Delete</button>
          </div>
        ))}
      </div>
    </div>
  );
}

function SnsView() {
  const [topics, setTopics] = useState<Array<{ name: string; arn: string }>>([]);
  const { showToast } = useToast();

  async function load() {
    const data = await fetchSnsTopics();
    setTopics(data.topics);
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
    const form = new FormData(event.currentTarget);
    const topicName = String(form.get("topicName") || "");
    const bodyRaw = String(form.get("body") || "{}");
    try {
      await publishSnsMessage(topicName, { body: JSON.parse(bodyRaw), headers: {} });
      showToast(`Published to ${topicName}`, "success");
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function onSubscribe(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    try {
      const topicName = String(form.get("topicName") || "");
      const queueName = String(form.get("queueName") || "");
      await subscribeSqsToSns({ topicName, queueName });
      showToast(`Subscribed ${queueName} to ${topicName}`, "success");
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  async function remove(topicName: string) {
    try {
      await deleteSnsTopic(topicName);
      showToast(`Topic deleted: ${topicName}`, "success");
      await load();
    } catch (err) {
      showToast(err instanceof Error ? err.message : "Error", "error");
    }
  }

  return (
    <div className="service-view">
      <h2>SNS Topic Management</h2>
      <form className="inline-form" onSubmit={onCreate}>
        <input name="topicName" required placeholder="topic-name" />
        <button type="submit">Create Topic</button>
      </form>
      <form className="inline-form" onSubmit={onPublish}>
        <input name="topicName" required placeholder="topic" />
        <input name="body" defaultValue='{"event":"order-created"}' />
        <button type="submit">Publish</button>
      </form>
      <form className="inline-form" onSubmit={onSubscribe}>
        <input name="topicName" required placeholder="topic" />
        <input name="queueName" required placeholder="queue" />
        <button type="submit">Subscribe SQS</button>
      </form>
      <div className="table">
        {topics.map((topic) => (
          <div className="table-row" key={topic.arn}>
            <span>{topic.name}</span>
            <button className="danger" onClick={() => remove(topic.name)}>Delete</button>
          </div>
        ))}
      </div>
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

  useEffect(() => {
    localStorage.setItem("aws-manager:refresh-ms", String(refreshMs));
  }, [refreshMs]);

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
    <AppShell health={health} refreshMs={refreshMs} setRefreshMs={setRefreshMs}>
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


