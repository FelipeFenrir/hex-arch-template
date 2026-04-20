import os
from urllib.parse import urlparse

import boto3

from .config import DEFAULT_AWS_ENDPOINT_URL, DEFAULT_AWS_REGION
from .utils import normalize_sqs_endpoint


def _resolve_endpoint(url: str | None = None) -> str | None:
    """Resolve the effective AWS service endpoint URL.

    Prefers the default emulator endpoint when the given URL resolves to
    localhost (common with local AWS emulators running inside Docker).
    """
    default_endpoint = normalize_sqs_endpoint(os.getenv("AWS_ENDPOINT_URL", DEFAULT_AWS_ENDPOINT_URL))
    endpoint = normalize_sqs_endpoint(url)

    # Resource URLs can come back as localhost from emulators; inside Docker, prefer service endpoint.
    if endpoint:
        host = (urlparse(endpoint).hostname or "").lower()
        if host in {"localhost", "127.0.0.1"} and default_endpoint:
            endpoint = default_endpoint

    return endpoint or default_endpoint


def _build_client(service_name: str, endpoint_url: str | None = None):
    """Create a boto3 client for the given AWS service."""
    return boto3.client(
        service_name,
        region_name=os.getenv("AWS_DEFAULT_REGION", DEFAULT_AWS_REGION),
        endpoint_url=endpoint_url,
    )
