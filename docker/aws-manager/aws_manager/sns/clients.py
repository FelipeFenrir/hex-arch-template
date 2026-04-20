from ..clients import _build_client, _resolve_endpoint


def build_sns_client(url: str | None = None):
    """Create a boto3 SNS client, resolving the endpoint for local emulators."""
    return _build_client("sns", _resolve_endpoint(url))

