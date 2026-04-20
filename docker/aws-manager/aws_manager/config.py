import os

DEFAULT_AWS_ENDPOINT_URL = "http://ministack:4566"
DEFAULT_AWS_REGION = "us-east-1"
DEFAULT_MINISTACK_HEALTH_PATH = "/_ministack/health"
DEBUG_TRUE_VALUES = {"1", "true", "yes", "on"}


def is_debug_enabled() -> bool:
    return str(os.getenv("FLASK_DEBUG", "0")).strip().lower() in DEBUG_TRUE_VALUES


def _build_health_url(base_url: str) -> str:
    return base_url.rstrip("/") + DEFAULT_MINISTACK_HEALTH_PATH


def ministack_health_url_candidates() -> list[str]:
    """Return health endpoint candidates for both compose and host executions."""
    explicit_health_url = str(os.getenv("MINISTACK_HEALTH_URL", "")).strip()
    aws_endpoint_url = str(os.getenv("AWS_ENDPOINT_URL", DEFAULT_AWS_ENDPOINT_URL)).strip() or DEFAULT_AWS_ENDPOINT_URL

    candidates = [
        explicit_health_url,
        _build_health_url(aws_endpoint_url),
        "http://localhost:4566/_ministack/health",
        "http://127.0.0.1:4566/_ministack/health",
    ]

    # Preserve order while deduplicating empty values.
    return [url for index, url in enumerate(candidates) if url and url not in candidates[:index]]


