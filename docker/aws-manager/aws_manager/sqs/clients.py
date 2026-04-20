from ..clients import _build_client, _resolve_endpoint


def build_sqs_client(url: str | None = None):
    """Create a boto3 SQS client, resolving the endpoint for local emulators."""
    return _build_client("sqs", _resolve_endpoint(url))

