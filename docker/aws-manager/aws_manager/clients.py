import os

import boto3

from .config import DEFAULT_AWS_REGION, _resolve_active_endpoint
from .utils import normalize_sqs_endpoint


def _resolve_endpoint(url: str | None = None) -> str | None:
    """Resolve the effective AWS service endpoint URL.

    If an explicit URL is provided, use it directly.
    Otherwise probe-detect the active MiniStack endpoint.
    """
    if url and url.strip():
        return url.strip()
    return _resolve_active_endpoint()


def _build_client(service_name: str, endpoint_url: str | None = None):
    """Create a boto3 client for the given AWS service."""
    aws_access_key = os.getenv("AWS_ACCESS_KEY_ID", "test")
    aws_secret_key = os.getenv("AWS_SECRET_ACCESS_KEY", "test")

    return boto3.client(
        service_name,
        region_name=os.getenv("AWS_DEFAULT_REGION", DEFAULT_AWS_REGION),
        endpoint_url=endpoint_url,
        aws_access_key_id=aws_access_key,
        aws_secret_access_key=aws_secret_key,
    )
