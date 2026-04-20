import json
from urllib.parse import urlparse


EMULATOR_HOSTS = {"localstack", "ministack", "localhost", "127.0.0.1"}


def normalize_sqs_endpoint(url: str | None) -> str | None:
    if not url:
        return None

    parsed = urlparse(url)
    host = (parsed.hostname or "").lower()

    if host in EMULATOR_HOSTS:
        scheme = parsed.scheme or "http"
        return f"{scheme}://{parsed.netloc}"

    return None


def parse_json_field(value: str | None, field_name: str) -> dict:
    if value is None or str(value).strip() == "":
        return {}

    try:
        return json.loads(value)
    except Exception as exc:
        raise ValueError(f"JSON invalido em '{field_name}': {exc}") from exc

