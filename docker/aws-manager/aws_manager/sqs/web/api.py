from flask import Blueprint, jsonify, request

import json

from ..clients import build_sqs_client
from ..services import (
    filter_messages,
    get_queue_runtime_summary,
    list_available_queues,
    update_queue_runtime_attributes,
)

sqs_api_blueprint = Blueprint("sqs_api", __name__)


# ---------------------------------------------------------------------------
# Queue listing / management
# ---------------------------------------------------------------------------

@sqs_api_blueprint.route("/monitor/queues", methods=["GET"])
def monitor_queues():
    try:
        return jsonify({"queues": list_available_queues()})
    except Exception as exc:
        return jsonify({"queues": [], "error": str(exc)}), 500


@sqs_api_blueprint.route("/api/sqs/queues", methods=["GET"])
def sqs_list_queues_api():
    try:
        return jsonify({"queues": list_available_queues()})
    except Exception as exc:
        return jsonify({"queues": [], "error": str(exc)}), 500


@sqs_api_blueprint.route("/api/sqs/queues", methods=["POST"])
def sqs_create_queue_api():
    body = request.json or {}
    queue_name = str(body.get("queueName") or "").strip()
    is_fifo = bool(body.get("isFifo"))
    content_based_dedup = bool(body.get("contentBasedDeduplication"))

    if not queue_name:
        return jsonify({"status": "invalid", "error": "queueName is required"}), 400

    if is_fifo and not queue_name.endswith(".fifo"):
        return jsonify({"status": "invalid", "error": "FIFO queue name must end with .fifo"}), 400

    attributes = {}
    if is_fifo:
        attributes["FifoQueue"] = "true"
        if content_based_dedup:
            attributes["ContentBasedDeduplication"] = "true"

    try:
        sqs = build_sqs_client()
        response = sqs.create_queue(QueueName=queue_name, Attributes=attributes) if attributes else sqs.create_queue(QueueName=queue_name)
        return jsonify({"status": "created", "queueName": queue_name, "queueUrl": response.get("QueueUrl", "-")})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sqs_api_blueprint.route("/api/sqs/queues/<queue_name>", methods=["GET"])
def sqs_queue_detail_api(queue_name: str):
    try:
        sqs = build_sqs_client()
        queue_url = sqs.get_queue_url(QueueName=queue_name)["QueueUrl"]
        attrs = sqs.get_queue_attributes(
            QueueUrl=queue_url,
            AttributeNames=[
                "All",
            ],
        ).get("Attributes", {})
        return jsonify({"queueName": queue_name, "queueUrl": queue_url, "attributes": attrs})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sqs_api_blueprint.route("/api/sqs/queues/<queue_name>/summary", methods=["GET"])
def sqs_queue_summary_api(queue_name: str):
    try:
        summary = get_queue_runtime_summary(queue_name)
        if not summary:
            return jsonify({"status": "error", "error": f"Fila '{queue_name}' nao encontrada"}), 404
        return jsonify(summary)
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sqs_api_blueprint.route("/api/sqs/queues/<queue_name>", methods=["PUT"])
def sqs_update_queue_api(queue_name: str):
    body = request.json or {}
    visibility_timeout = body.get("visibilityTimeout")
    message_retention_period = body.get("messageRetentionPeriod")

    if visibility_timeout is None and message_retention_period is None:
        return jsonify({"status": "invalid", "error": "Informe ao menos um atributo para atualizar"}), 400

    try:
        summary = update_queue_runtime_attributes(
            queue_name,
            visibility_timeout=visibility_timeout,
            message_retention_period=message_retention_period,
        )
        if not summary:
            return jsonify({"status": "error", "error": f"Fila '{queue_name}' nao encontrada"}), 404
        return jsonify({"status": "updated", "queue": summary})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sqs_api_blueprint.route("/api/sqs/queues/<queue_name>", methods=["DELETE"])
def sqs_delete_queue_api(queue_name: str):
    force = str(request.args.get("force") or "false").strip().lower() in {"1", "true", "yes", "y"}

    try:
        summary = get_queue_runtime_summary(queue_name)
        if not summary:
            return jsonify({"status": "error", "error": f"Fila '{queue_name}' nao encontrada"}), 404

        if bool(summary.get("hasActiveMessages")) and not force:
            return (
                jsonify(
                    {
                        "status": "blocked",
                        "error": "Fila possui mensagens ativas. Confirme a exclusao forcada.",
                        "requiresConfirmation": True,
                        "queue": summary,
                    }
                ),
                409,
            )

        sqs = build_sqs_client()
        queue_url = str(summary.get("url"))
        sqs.delete_queue(QueueUrl=queue_url)
        return jsonify({"status": "deleted", "queueName": queue_name})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sqs_api_blueprint.route("/api/sqs/queues/<queue_name>/messages", methods=["GET"])
def sqs_receive_messages_api(queue_name: str):
    max_messages = int(request.args.get("maxMessages", 10))
    max_messages = max(1, min(max_messages, 10))

    try:
        sqs = build_sqs_client()
        queue_url = sqs.get_queue_url(QueueName=queue_name)["QueueUrl"]
        response = sqs.receive_message(
            QueueUrl=queue_url,
            MaxNumberOfMessages=max_messages,
            AttributeNames=["All"],
            MessageAttributeNames=["All"],
            VisibilityTimeout=1,
            WaitTimeSeconds=1,
        )
        return jsonify({"messages": response.get("Messages", [])})
    except Exception as exc:
        return jsonify({"messages": [], "error": str(exc)}), 500


@sqs_api_blueprint.route("/api/sqs/queues/<queue_name>/messages", methods=["POST"])
def sqs_send_message_api(queue_name: str):
    body = request.json or {}
    payload = body.get("body") or {}
    headers = body.get("headers") or {}

    if not isinstance(payload, dict):
        return jsonify({"status": "invalid", "error": "body must be a JSON object"}), 400
    if not isinstance(headers, dict):
        return jsonify({"status": "invalid", "error": "headers must be a JSON object"}), 400

    message_attributes = {
        key: {"StringValue": str(value), "DataType": "String"} for key, value in headers.items()
    }

    try:
        sqs = build_sqs_client()
        queue_url = sqs.get_queue_url(QueueName=queue_name)["QueueUrl"]
        response = sqs.send_message(
            QueueUrl=queue_url,
            MessageBody=json.dumps(payload),
            MessageAttributes=message_attributes,
        )
        return jsonify({"status": "sent", "messageId": response.get("MessageId", "-")})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sqs_api_blueprint.route("/monitor/available-queues", methods=["GET"])
def monitor_available_queues():
    try:
        return jsonify({"queues": list_available_queues()})
    except Exception as exc:
        return jsonify({"error": str(exc), "queues": []}), 500


@sqs_api_blueprint.route("/monitor/queues", methods=["POST"])
def add_monitor_queue():
    return jsonify(
        {
            "status": "noop",
            "message": "Todas as filas disponiveis ja sao monitoradas automaticamente.",
        }
    )


@sqs_api_blueprint.route("/monitor/queues/<name>", methods=["DELETE"])
def remove_monitor_queue(name: str):
    return jsonify(
        {
            "status": "noop",
            "message": f"A fila '{name}' continuara aparecendo enquanto existir no emulador.",
        }
    )


@sqs_api_blueprint.route("/monitor/queues/sqs", methods=["DELETE"])
def delete_sqs_queue():
    body = request.json or {}
    queue_url = body.get("url")

    if not queue_url:
        return jsonify({"status": "invalid", "error": "queue url is required"}), 400

    try:
        sqs = build_sqs_client(queue_url)
        sqs.delete_queue(QueueUrl=queue_url)
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500

    return jsonify({"status": "deleted"})


# ---------------------------------------------------------------------------
# Message inspection
# ---------------------------------------------------------------------------

@sqs_api_blueprint.route("/monitor/messages", methods=["GET"])
def monitor_messages():
    queue_url = request.args.get("queue")
    search = request.args.get("q", "").lower()
    header_key = request.args.get("headerKey", "").strip().lower()
    header_value = request.args.get("headerValue", "").strip().lower()

    sqs = build_sqs_client(queue_url)
    response = sqs.receive_message(
        QueueUrl=queue_url,
        MaxNumberOfMessages=10,
        AttributeNames=["All"],
        MessageAttributeNames=["All"],
        VisibilityTimeout=1,
        WaitTimeSeconds=1,
    )

    messages = response.get("Messages", [])
    return jsonify(filter_messages(messages, search, header_key, header_value))


@sqs_api_blueprint.route("/monitor/queue-stats", methods=["GET"])
def monitor_queue_stats():
    queue_url = request.args.get("queue")
    if not queue_url:
        return jsonify({"error": "queue is required"}), 400

    try:
        sqs = build_sqs_client(queue_url)
        response = sqs.get_queue_attributes(
            QueueUrl=queue_url,
            AttributeNames=[
                "ApproximateNumberOfMessages",
                "ApproximateNumberOfMessagesNotVisible",
                "ApproximateNumberOfMessagesDelayed",
            ],
        )
        attrs = response.get("Attributes", {})
        return jsonify(
            {
                "visible": int(attrs.get("ApproximateNumberOfMessages", 0)),
                "inflight": int(attrs.get("ApproximateNumberOfMessagesNotVisible", 0)),
                "delayed": int(attrs.get("ApproximateNumberOfMessagesDelayed", 0)),
            }
        )
    except Exception as exc:
        return jsonify({"error": str(exc)}), 500


@sqs_api_blueprint.route("/monitor/read", methods=["POST"])
def read_message():
    queue = request.json["queue"]
    receipt = request.json["receipt"]

    sqs = build_sqs_client(queue)
    sqs.change_message_visibility(QueueUrl=queue, ReceiptHandle=receipt, VisibilityTimeout=30)
    return jsonify({"status": "locked"})


@sqs_api_blueprint.route("/monitor/delete", methods=["POST"])
def delete_message():
    queue = request.json["queue"]
    receipt = request.json["receipt"]

    sqs = build_sqs_client(queue)
    sqs.delete_message(QueueUrl=queue, ReceiptHandle=receipt)
    return jsonify({"status": "deleted"})

