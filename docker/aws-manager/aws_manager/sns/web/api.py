import json

from flask import Blueprint, jsonify, request

from ..clients import build_sns_client
from ..services import (
    get_topic_arn_by_name,
    get_topic_runtime_summary,
    list_available_topics,
    list_topic_subscriptions,
    update_topic_runtime_attributes,
)
from ...sqs.services import ensure_sqs_policy_for_sns_subscription, get_queue_info_by_name

sns_api_blueprint = Blueprint("sns_api", __name__)


# ---------------------------------------------------------------------------
# Topics
# ---------------------------------------------------------------------------

@sns_api_blueprint.route("/sns/topics", methods=["GET"])
def sns_topics():
    try:
        return jsonify({"topics": list_available_topics()})
    except Exception as exc:
        return jsonify({"topics": [], "error": str(exc)}), 500


@sns_api_blueprint.route("/api/sns/topics", methods=["GET"])
def sns_topics_api():
    try:
        return jsonify({"topics": list_available_topics()})
    except Exception as exc:
        return jsonify({"topics": [], "error": str(exc)}), 500


@sns_api_blueprint.route("/api/sns/topics", methods=["POST"])
def sns_create_topic_api():
    body = request.json or {}
    topic_name = (body.get("topicName") or "").strip()
    is_fifo = bool(body.get("isFifo"))
    content_based_dedup = bool(body.get("contentBasedDeduplication"))

    if not topic_name:
        return jsonify({"status": "invalid", "error": "topicName is required"}), 400

    if is_fifo and not topic_name.endswith(".fifo"):
        return jsonify({"status": "invalid", "error": "FIFO topic name must end with .fifo"}), 400

    attributes = {}
    if is_fifo:
        attributes["FifoTopic"] = "true"
        if content_based_dedup:
            attributes["ContentBasedDeduplication"] = "true"

    try:
        sns = build_sns_client()
        response = sns.create_topic(Name=topic_name, Attributes=attributes)
        return jsonify({"status": "created", "topicName": topic_name, "topicArn": response.get("TopicArn", "-")})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sns_api_blueprint.route("/api/sns/topics/<topic_name>", methods=["DELETE"])
def sns_delete_topic_api(topic_name: str):
    force = str(request.args.get("force") or "false").strip().lower() in {"1", "true", "yes", "y"}

    try:
        summary = get_topic_runtime_summary(topic_name)
        if not summary:
            return jsonify({"status": "error", "error": f"Topico '{topic_name}' nao encontrado"}), 404

        if bool(summary.get("hasActiveSubscriptions")) and not force:
            return (
                jsonify(
                    {
                        "status": "blocked",
                        "error": "Topico possui subscriptions ativas. Confirme a exclusao forcada.",
                        "requiresConfirmation": True,
                        "topic": summary,
                    }
                ),
                409,
            )

        topic_arn = str(summary.get("arn"))
        sns = build_sns_client(topic_arn)
        sns.delete_topic(TopicArn=topic_arn)
        return jsonify({"status": "deleted", "topicName": topic_name})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sns_api_blueprint.route("/api/sns/topics/<topic_name>/summary", methods=["GET"])
def sns_topic_summary_api(topic_name: str):
    try:
        summary = get_topic_runtime_summary(topic_name)
        if not summary:
            return jsonify({"status": "error", "error": f"Topico '{topic_name}' nao encontrado"}), 404
        return jsonify(summary)
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sns_api_blueprint.route("/api/sns/topics/<topic_name>", methods=["PUT"])
def sns_update_topic_api(topic_name: str):
    body = request.json or {}
    display_name = body.get("displayName")

    try:
        summary = update_topic_runtime_attributes(topic_name, display_name=display_name)
        if not summary:
            return jsonify({"status": "error", "error": f"Topico '{topic_name}' nao encontrado"}), 404
        return jsonify({"status": "updated", "topic": summary})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sns_api_blueprint.route("/api/sns/topics/<topic_name>/publish", methods=["POST"])
def sns_publish_message_api(topic_name: str):
    body = request.json or {}
    payload = body.get("body") or {}
    headers = body.get("headers") or {}
    subject = str(body.get("subject") or "").strip()
    message_group_id = str(body.get("messageGroupId") or "default").strip() or "default"

    if not isinstance(payload, dict):
        return jsonify({"status": "invalid", "error": "body must be a JSON object"}), 400
    if not isinstance(headers, dict):
        return jsonify({"status": "invalid", "error": "headers must be a JSON object"}), 400

    topic_arn = get_topic_arn_by_name(topic_name)
    if not topic_arn:
        return jsonify({"status": "error", "error": f"Topico '{topic_name}' nao encontrado"}), 404

    message_attributes = {
        key: {"DataType": "String", "StringValue": str(value)} for key, value in headers.items()
    }

    publish_kwargs = {
        "TopicArn": topic_arn,
        "Message": body.get("rawMessage") or json.dumps(payload),
        "MessageAttributes": message_attributes,
    }

    if subject:
        publish_kwargs["Subject"] = subject
    if topic_name.endswith(".fifo"):
        publish_kwargs["MessageGroupId"] = message_group_id

    try:
        sns = build_sns_client(topic_arn)
        response = sns.publish(**publish_kwargs)
        return jsonify({"status": "published", "messageId": response.get("MessageId", "-")})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@sns_api_blueprint.route("/api/sns/topics/<topic_name>/subscriptions", methods=["GET"])
def sns_topic_subscriptions_api(topic_name: str):
    topic_arn = get_topic_arn_by_name(topic_name)
    if not topic_arn:
        return jsonify({"subscriptions": [], "error": f"Topico '{topic_name}' nao encontrado"}), 404

    try:
        return jsonify({"subscriptions": list_topic_subscriptions(topic_arn)})
    except Exception as exc:
        return jsonify({"subscriptions": [], "error": str(exc)}), 500


@sns_api_blueprint.route("/sns/topics", methods=["DELETE"])
def sns_delete_topic():
    body = request.json or {}
    topic_arn = body.get("topicArn")

    if not topic_arn:
        return jsonify({"status": "invalid", "error": "topicArn is required"}), 400

    try:
        sns = build_sns_client(topic_arn)
        sns.delete_topic(TopicArn=topic_arn)
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500

    return jsonify({"status": "deleted"})


# ---------------------------------------------------------------------------
# Subscriptions
# ---------------------------------------------------------------------------

@sns_api_blueprint.route("/sns/subscriptions", methods=["GET"])
def sns_subscriptions():
    topic_arn = request.args.get("topicArn")
    if not topic_arn:
        return jsonify({"subscriptions": [], "error": "topicArn is required"}), 400

    try:
        return jsonify({"subscriptions": list_topic_subscriptions(topic_arn)})
    except Exception as exc:
        return jsonify({"subscriptions": [], "error": str(exc)}), 500


@sns_api_blueprint.route("/api/sns/subscriptions/sqs", methods=["POST"])
def sns_subscribe_sqs_api():
    """JSON API to subscribe an SQS queue to an SNS topic.

    Uses /api/sns/... prefix to avoid clash with the HTML form handler at
    /sns/subscriptions/sqs (POST).
    """
    body = request.json or {}
    topic_name = (body.get("topicName") or "").strip()
    queue_name = (body.get("queueName") or "").strip()

    if not topic_name or not queue_name:
        return jsonify({"status": "invalid", "error": "topicName and queueName are required"}), 400

    topic_arn = get_topic_arn_by_name(topic_name)
    if not topic_arn:
        return jsonify({"status": "error", "error": f"Topico '{topic_name}' nao encontrado"}), 404

    queue_info = get_queue_info_by_name(queue_name)
    if not queue_info:
        return jsonify({"status": "error", "error": f"Fila '{queue_name}' nao encontrada"}), 404

    try:
        ensure_sqs_policy_for_sns_subscription(queue_info["url"], queue_info["arn"], topic_arn)
        sns = build_sns_client(topic_arn)
        response = sns.subscribe(TopicArn=topic_arn, Protocol="sqs", Endpoint=queue_info["arn"])
        return jsonify({"status": "subscribed", "subscriptionArn": response.get("SubscriptionArn", "-")})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500

