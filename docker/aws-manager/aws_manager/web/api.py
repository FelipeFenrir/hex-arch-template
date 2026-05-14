import json
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime, timezone
from urllib.request import Request, urlopen

from flask import Blueprint, current_app, jsonify, request

from ..cache import cache
from ..clients import _build_client, _resolve_endpoint
from ..config import DEFAULT_AWS_REGION, ministack_health_url_candidates
from ..registry import (
    DESCRIBE_REGISTRY,
    ID_FIELDS,
    LIST_PARAMS,
    METHOD_KWARGS,
    PREFERRED_ID_FIELD,
    SERVICE_REGISTRY,
)

api_blueprint = Blueprint("api", __name__)
_start_time = time.time()

ONLINE_WORDS = {"ok", "up", "online", "healthy", "running", "available", "enabled", "active", "ready"}
OFFLINE_WORDS = {"down", "offline", "unhealthy", "stopped", "unavailable", "disabled", "error", "failed"}


def _status_from_value(value):
    if isinstance(value, bool):
        return value

    if isinstance(value, str):
        normalized = value.strip().lower()
        if normalized in ONLINE_WORDS:
            return True
        if normalized in OFFLINE_WORDS:
            return False
        for word in ONLINE_WORDS:
            if word in normalized:
                return True
        for word in OFFLINE_WORDS:
            if word in normalized:
                return False
        return None

    if isinstance(value, dict):
        for key in ("online", "healthy", "available", "status", "state"):
            if key in value:
                nested = _status_from_value(value[key])
                if nested is not None:
                    return nested
        return None

    if isinstance(value, list):
        nested = [_status_from_value(item) for item in value]
        resolved = [item for item in nested if item is not None]
        if not resolved:
            return None
        return any(resolved)

    return None


def _find_service_status(payload, service_name: str):
    target = service_name.lower()

    def visit(node):
        if isinstance(node, dict):
            for key, value in node.items():
                if str(key).strip().lower() == target:
                    status = _status_from_value(value)
                    if status is not None:
                        return status
                nested = visit(value)
                if nested is not None:
                    return nested

        if isinstance(node, list):
            for item in node:
                nested = visit(item)
                if nested is not None:
                    return nested

        if isinstance(node, str) and node.strip().lower() == target:
            return True

        return None

    return visit(payload)


def _serialize(obj):
    if isinstance(obj, dict):
        return {k: _serialize(v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [_serialize(v) for v in obj]
    if hasattr(obj, "isoformat"):
        return obj.isoformat()
    if isinstance(obj, bytes):
        return obj.decode("utf-8", errors="replace")
    return obj


def _count_items(resp, response_key: str) -> int:
    items = resp.get(response_key, [])
    if isinstance(items, dict) and "Items" in items:
        return len(items.get("Items", []) or [])
    if isinstance(items, list):
        return len(items)
    return 0


def _extract_id(item, preferred_field: str | None = None) -> str:
    if isinstance(item, str):
        return item
    if isinstance(item, dict):
        if preferred_field and preferred_field in item:
            return str(item[preferred_field])
        for field in ID_FIELDS:
            if field in item:
                return str(item[field])
        for value in item.values():
            if isinstance(value, str):
                return value
    return str(item)


def _summarize_item(item, preferred_field: str | None = None) -> dict:
    if isinstance(item, str):
        return {"id": item}
    if isinstance(item, dict):
        summary = {"id": _extract_id(item, preferred_field)}
        for key, value in item.items():
            if isinstance(value, (str, int, float, bool)) or value is None:
                summary[key] = value
            elif hasattr(value, "isoformat"):
                summary[key] = value.isoformat()
        return summary
    return {"id": str(item)}


def _probe_service(service: str, endpoint_url: str) -> tuple[str, dict]:
    entries = SERVICE_REGISTRY.get(service)
    if not entries:
        return service, {"status": "unavailable", "resources": {}}

    resources: dict[str, int] = {}
    try:
        for resource_type, boto3_service, method_name, response_key in entries:
            client = _build_client(boto3_service, endpoint_url)
            method = getattr(client, method_name)
            kwargs = METHOD_KWARGS.get((boto3_service, method_name), {})
            try:
                resp = method(**kwargs)
                resources[resource_type] = _count_items(resp, response_key)
            except Exception:
                resources[resource_type] = 0
        return service, {"status": "available", "resources": resources}
    except Exception:
        return service, {"status": "unavailable", "resources": {}}


def _effective_endpoint() -> str:
    """Return endpoint URL: explicit query param > probed active endpoint."""
    requested = (request.args.get("endpoint_url") or "").strip() or None
    return _resolve_endpoint(requested)


@api_blueprint.get("/api/health")
def api_health():
    return jsonify(
        {
            "status": "ok",
            "endpoint_url": _effective_endpoint(),
            "region": DEFAULT_AWS_REGION,
            "services_count": len(SERVICE_REGISTRY),
            "uptime_seconds": round(time.time() - _start_time, 1),
        }
    )


@api_blueprint.get("/api/endpoints")
def api_endpoints():
    routes = []
    for rule in current_app.url_map.iter_rules():
        if rule.endpoint == "static":
            continue

        methods = sorted(method for method in rule.methods if method not in {"HEAD", "OPTIONS"})
        if not methods:
            continue

        routes.append(
            {
                "path": str(rule),
                "methods": methods,
                "endpoint": rule.endpoint,
            }
        )

    routes.sort(key=lambda item: (item["path"], ",".join(item["methods"])))
    return jsonify({"routes": routes})


@api_blueprint.get("/api/ministack/health")
def ministack_health_api():
    last_error = "MiniStack health endpoint unavailable"

    for health_url in ministack_health_url_candidates():
        try:
            req = Request(health_url, headers={"Accept": "application/json"})
            with urlopen(req, timeout=3) as response:
                payload = json.loads(response.read().decode("utf-8"))

            services = {}
            for service_name in SERVICE_REGISTRY.keys():
                status = _find_service_status(payload, service_name)
                services[service_name] = bool(status) if status is not None else False

            return jsonify(
                {
                    "online": True,
                    "services": services,
                    "checked_at": datetime.now(timezone.utc).isoformat(),
                    "source": health_url,
                    "raw": payload,
                }
            )
        except Exception as exc:
            last_error = str(exc)

    return (
        jsonify(
            {
                "online": False,
                "services": {name: False for name in SERVICE_REGISTRY},
                "checked_at": datetime.now(timezone.utc).isoformat(),
                "error": last_error,
            }
        ),
        503,
    )


@api_blueprint.get("/api/stats")
def api_stats():
    endpoint_url = _effective_endpoint()
    cache_key = f"{endpoint_url}:stats"
    cached = cache.get(cache_key)
    if cached is not None:
        return jsonify(cached)

    services: dict[str, dict] = {}
    total_resources = 0
    service_names = sorted(SERVICE_REGISTRY.keys())

    with ThreadPoolExecutor(max_workers=min(len(service_names), 10)) as executor:
        futures = {executor.submit(_probe_service, name, endpoint_url): name for name in service_names}
        for future in as_completed(futures):
            service_name, result = future.result()
            services[service_name] = result
            total_resources += sum(result["resources"].values())

    response = {
        "services": dict(sorted(services.items())),
        "total_resources": total_resources,
        "uptime_seconds": round(time.time() - _start_time, 1),
    }
    cache.set(cache_key, response, ttl_seconds=5)
    return jsonify(response)


@api_blueprint.get("/api/resources/<service>")
def api_list_resources(service: str):
    endpoint_url = _effective_endpoint()
    cache_key = f"{endpoint_url}:resources:{service}"
    cached = cache.get(cache_key)
    if cached is not None:
        return jsonify(cached)

    entries = SERVICE_REGISTRY.get(service)
    if not entries:
        return jsonify({"error": f"Unknown service: {service}"}), 404

    resources: dict[str, list[dict]] = {}
    for resource_type, boto3_service, method_name, response_key in entries:
        try:
            client = _build_client(boto3_service, endpoint_url)
            method = getattr(client, method_name)
            kwargs = METHOD_KWARGS.get((boto3_service, method_name), {})
            resp = method(**kwargs)
            items = resp.get(response_key, [])
            if isinstance(items, dict) and "Items" in items:
                items = items.get("Items", []) or []
            preferred = PREFERRED_ID_FIELD.get((service, resource_type))
            resources[resource_type] = [_summarize_item(item, preferred) for item in items]
        except Exception as exc:
            print(f"[aws-manager] ERROR listing {service}/{resource_type}: {exc}")
            resources[resource_type] = []

    result = {"service": service, "resources": resources}
    cache.set(cache_key, result, ttl_seconds=5)
    return jsonify(result)


@api_blueprint.get("/api/resources/<service>/<res_type>/<path:res_id>")
def api_resource_detail(service: str, res_type: str, res_id: str):
    endpoint_url = _effective_endpoint()
    cache_key = f"{endpoint_url}:detail:{service}:{res_type}:{res_id}"
    cached = cache.get(cache_key)
    if cached is not None:
        return jsonify(cached)

    lookup = DESCRIBE_REGISTRY.get((service, res_type))
    if not lookup:
        return jsonify({"error": f"No detail lookup registered for {service}/{res_type}"}), 404

    boto3_service, method_name, id_param, response_key = lookup

    try:
        client = _build_client(boto3_service, endpoint_url)
        method = getattr(client, method_name)
        if id_param in LIST_PARAMS:
            resp = method(**{id_param: [res_id]})
        else:
            resp = method(**{id_param: res_id})

        if isinstance(resp, dict):
            resp.pop("ResponseMetadata", None)

        detail = resp.get(response_key, resp) if response_key is not None else resp
        result = {
            "service": service,
            "type": res_type,
            "id": res_id,
            "detail": _serialize(detail),
        }
        cache.set(cache_key, result, ttl_seconds=5)
        return jsonify(result)
    except Exception as exc:
        return jsonify({"error": str(exc)}), 500


