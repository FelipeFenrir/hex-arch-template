import json
from datetime import datetime, timezone
from urllib.request import Request, urlopen

from flask import Blueprint, current_app, jsonify, render_template

from ..config import ministack_health_url_candidates

home_blueprint = Blueprint("home", __name__)

ONLINE_WORDS = {"ok", "up", "online", "healthy", "running", "available", "enabled", "active", "ready"}
OFFLINE_WORDS = {"down", "offline", "unhealthy", "stopped", "unavailable", "disabled", "error", "failed"}


@home_blueprint.route("/")
def index():
    return render_template("index.html", result=None)


@home_blueprint.route("/apis")
def api_catalog():
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
    return render_template("apis.html", routes=routes)


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
        for key in ("online", "healthy", "available"):
            if key in value:
                nested = _status_from_value(value[key])
                if nested is not None:
                    return nested

        for key in ("status", "state"):
            if key in value:
                nested = _status_from_value(value[key])
                if nested is not None:
                    return nested

        return None

    if isinstance(value, list):
        if not value:
            return None
        nested_values = [_status_from_value(item) for item in value]
        nested_values = [item for item in nested_values if item is not None]
        if not nested_values:
            return None
        return any(nested_values)

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


@home_blueprint.route("/ministack/health", methods=["GET"])
def ministack_health():
    last_error = "MiniStack health endpoint unavailable"

    for health_url in ministack_health_url_candidates():
        try:
            request = Request(health_url, headers={"Accept": "application/json"})
            with urlopen(request, timeout=3) as response:
                payload = json.loads(response.read().decode("utf-8"))

            sqs_status = _find_service_status(payload, "sqs")
            sns_status = _find_service_status(payload, "sns")
            s3_status = _find_service_status(payload, "s3")
            dynamodb_status = _find_service_status(payload, "dynamodb")
            lambda_status = _find_service_status(payload, "lambda")
            cloudwatch_status = _find_service_status(payload, "cloudwatch")
            ecs_status = _find_service_status(payload, "ecs")
            ec2_status = _find_service_status(payload, "ec2")
            iam_status = _find_service_status(payload, "iam")

            return jsonify(
                {
                    "online": True,
                    "services": {
                        "sqs": bool(sqs_status) if sqs_status is not None else False,
                        "sns": bool(sns_status) if sns_status is not None else False,
                        "s3": bool(s3_status) if s3_status is not None else False,
                        "dynamodb": bool(dynamodb_status) if dynamodb_status is not None else False,
                        "lambda": bool(lambda_status) if lambda_status is not None else False,
                        "cloudwatch": bool(cloudwatch_status) if cloudwatch_status is not None else False,
                        "ecs": bool(ecs_status) if ecs_status is not None else False,
                        "ec2": bool(ec2_status) if ec2_status is not None else False,
                        "iam": bool(iam_status) if iam_status is not None else False,
                    },
                    "checked_at": datetime.now(timezone.utc).isoformat(),
                    "source": health_url,
                    "raw": payload,
                }
            )
        except Exception as exc:
            last_error = str(exc)

    return jsonify(
        {
            "online": False,
            "services": {
                "sqs": False,
                "sns": False,
                "s3": False,
                "dynamodb": False,
                "lambda": False,
                "cloudwatch": False,
                "ecs": False,
                "ec2": False,
                "iam": False,
            },
            "checked_at": datetime.now(timezone.utc).isoformat(),
            "error": last_error,
        }
    ), 503

