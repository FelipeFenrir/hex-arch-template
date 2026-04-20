from ..clients import _build_client, _resolve_endpoint


def build_s3_client(url: str | None = None):
    """Create a boto3 S3 client, resolving the endpoint for local emulators."""
    return _build_client("s3", _resolve_endpoint(url))

