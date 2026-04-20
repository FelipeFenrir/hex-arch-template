from flask import Blueprint, jsonify, request

from ..clients import build_sqs_client
from ..services import filter_messages, list_available_queues

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

