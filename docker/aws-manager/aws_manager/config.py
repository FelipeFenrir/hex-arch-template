import os
import socket

DEFAULT_AWS_ENDPOINT_URL = "http://ministack:4566"
DEFAULT_AWS_REGION = "us-east-1"
DEFAULT_MINISTACK_HEALTH_PATH = "/_ministack/health"
DEBUG_TRUE_VALUES = {"1", "true", "yes", "on"}

_ENDPOINT_CANDIDATES = [
    "http://ministack:4566",
    "http://localhost:4566",
    "http://127.0.0.1:4566",
]

# Cache the probed endpoint at module load time.
_probed_endpoint: str | None = None


def _is_reachable(host: str, port: int, timeout: float = 1.0) -> bool:
    """Check if a TCP connection can be established to host:port."""
    try:
        with socket.create_connection((host, port), timeout=timeout):
            return True
    except OSError:
        return False


def _resolve_active_endpoint() -> str:
    """Return the first reachable MiniStack endpoint.

    Checks candidates in order:
      1. ``AWS_ENDPOINT_URL`` env var (if set)
      2. ``http://ministack:4566``  (Docker Compose hostname)
      3. ``http://localhost:4566``  (host execution)
      4. ``http://127.0.0.1:4566`` (host execution – numeric)
    Falls back to the first candidate without probing on error.
    """
    global _probed_endpoint
    if _probed_endpoint:
        return _probed_endpoint

    env_url = os.getenv("AWS_ENDPOINT_URL", "").strip()
    candidates = ([env_url] if env_url else []) + _ENDPOINT_CANDIDATES

    for url in candidates:
        if not url:
            continue
        # Parse host and port from the URL
        url_stripped = url.rstrip("/")
        try:
            if "://" in url_stripped:
                _, authority = url_stripped.split("://", 1)
            else:
                authority = url_stripped
            if ":" in authority:
                host, port_str = authority.rsplit(":", 1)
                port = int(port_str)
            else:
                host = authority
                port = 80
        except Exception:
            continue

        if _is_reachable(host, port):
            _probed_endpoint = url
            print(f"[aws-manager] Resolved active endpoint: {url}")
            return url

    # None reachable – default to env or first candidate
    fallback = env_url or _ENDPOINT_CANDIDATES[0]
    _probed_endpoint = fallback
    print(f"[aws-manager] No reachable endpoint found; defaulting to: {fallback}")
    return fallback


def is_debug_enabled() -> bool:
    return str(os.getenv("FLASK_DEBUG", "0")).strip().lower() in DEBUG_TRUE_VALUES


def _build_health_url(base_url: str) -> str:
    return base_url.rstrip("/") + DEFAULT_MINISTACK_HEALTH_PATH


def ministack_health_url_candidates() -> list[str]:
    """Return health endpoint candidates for both compose and host executions."""
    explicit_health_url = str(os.getenv("MINISTACK_HEALTH_URL", "")).strip()
    active_endpoint = _resolve_active_endpoint()

    candidates = [
        explicit_health_url,
        _build_health_url(active_endpoint),
        "http://localhost:4566/_ministack/health",
        "http://127.0.0.1:4566/_ministack/health",
    ]

    # Preserve order while deduplicating empty values.
    return [url for index, url in enumerate(candidates) if url and url not in candidates[:index]]

