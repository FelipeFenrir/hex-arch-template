from flask import Blueprint, jsonify, request

from ..clients import build_sns_client
from ..services import get_topic_arn_by_name, list_available_topics, list_topic_subscriptions
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

