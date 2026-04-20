import json

from flask import Blueprint, redirect, render_template, request, url_for

from ..clients import build_sns_client
from ..services import get_topic_arn_by_name
from ...sqs.services import ensure_sqs_policy_for_sns_subscription, get_queue_info_by_name
from ...utils import parse_json_field

sns_pages_blueprint = Blueprint("sns_pages", __name__)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def render_sns_page(
    active_tab: str = "monitor",
    create_result: str | None = None,
    publish_result: str | None = None,
    subscribe_result: str | None = None,
    create_form: dict | None = None,
    publish_form: dict | None = None,
    subscribe_form: dict | None = None,
):
    return render_template(
        "sns.html",
        active_tab=active_tab,
        create_result=create_result,
        publish_result=publish_result,
        subscribe_result=subscribe_result,
        create_form=create_form or {},
        publish_form=publish_form or {},
        subscribe_form=subscribe_form or {},
    )


# ---------------------------------------------------------------------------
# SNS workspace page
# ---------------------------------------------------------------------------

@sns_pages_blueprint.route("/sns")
def sns():
    active_tab = (request.args.get("tab") or "monitor").strip().lower()
    if active_tab not in {"monitor", "create", "publish", "subscribe"}:
        active_tab = "monitor"
    return render_sns_page(active_tab=active_tab)


# ---------------------------------------------------------------------------
# Create topic
# ---------------------------------------------------------------------------

@sns_pages_blueprint.route("/sns/topics/create", methods=["GET"])
def sns_create_topic_page():
    return redirect(url_for("sns_pages.sns", tab="create"))


@sns_pages_blueprint.route("/sns/topics/create", methods=["POST"])
def sns_create_topic_submit():
    topic_name = (request.form.get("topic_name") or "").strip()
    is_fifo = bool(request.form.get("is_fifo"))
    content_based_dedup = bool(request.form.get("content_based_dedup"))

    create_form = {
        "topic_name": topic_name,
        "is_fifo": is_fifo,
        "content_based_dedup": content_based_dedup,
    }

    if not topic_name:
        return render_sns_page(active_tab="create", create_result="Informe o nome do topico.", create_form=create_form)

    if is_fifo and not topic_name.endswith(".fifo"):
        return render_sns_page(
            active_tab="create",
            create_result="Topicos FIFO devem terminar com '.fifo'.",
            create_form=create_form,
        )

    attributes = {}
    if is_fifo:
        attributes["FifoTopic"] = "true"
        if content_based_dedup:
            attributes["ContentBasedDeduplication"] = "true"

    try:
        sns_client = build_sns_client()
        response = sns_client.create_topic(Name=topic_name, Attributes=attributes)
        topic_arn = response.get("TopicArn", "-")
        return render_sns_page(
            active_tab="create",
            create_result=f"Topico criado com sucesso: {topic_name} ({topic_arn})",
            create_form={},
        )
    except Exception as exc:
        return render_sns_page(
            active_tab="create",
            create_result=f"Erro ao criar topico: {exc}",
            create_form=create_form,
        )


# ---------------------------------------------------------------------------
# Publish message
# ---------------------------------------------------------------------------

@sns_pages_blueprint.route("/sns/messages/publish", methods=["GET"])
def sns_publish_page():
    return redirect(url_for("sns_pages.sns", tab="publish"))


@sns_pages_blueprint.route("/sns/messages/publish", methods=["POST"])
def sns_publish_submit():
    topic_name = (request.form.get("topic_name") or "").strip()
    subject = (request.form.get("subject") or "").strip()
    headers_text = request.form.get("headers", "{}")
    body_text = request.form.get("body", "")
    message_group_id = request.form.get("message_group_id") or "default"

    publish_form = {
        "topic_name": topic_name,
        "subject": subject,
        "headers": headers_text,
        "body": body_text,
        "message_group_id": message_group_id,
    }

    if not topic_name:
        return render_sns_page(active_tab="publish", publish_result="Informe o nome do topico.", publish_form=publish_form)

    topic_arn = get_topic_arn_by_name(topic_name)
    if not topic_arn:
        return render_sns_page(
            active_tab="publish",
            publish_result=f"Topico '{topic_name}' nao encontrado.",
            publish_form=publish_form,
        )

    try:
        headers_dict = parse_json_field(headers_text, "headers")
        body_dict = parse_json_field(body_text, "body")
    except ValueError as exc:
        return render_sns_page(active_tab="publish", publish_result=str(exc), publish_form=publish_form)

    message_attributes = {
        key: {"DataType": "String", "StringValue": str(value)} for key, value in headers_dict.items()
    }

    publish_kwargs: dict = {
        "TopicArn": topic_arn,
        "Message": json.dumps(body_dict),
        "MessageAttributes": message_attributes,
    }

    if subject:
        publish_kwargs["Subject"] = subject

    if topic_name.endswith(".fifo"):
        publish_kwargs["MessageGroupId"] = message_group_id or "default"

    try:
        sns_client = build_sns_client(topic_arn)
        response = sns_client.publish(**publish_kwargs)
        message_id = response.get("MessageId", "-")
        return render_sns_page(
            active_tab="publish",
            publish_result=f"Mensagem publicada com sucesso no topico '{topic_name}'. MessageId: {message_id}",
            publish_form={},
        )
    except Exception as exc:
        return render_sns_page(
            active_tab="publish",
            publish_result=f"Erro ao publicar mensagem: {exc}",
            publish_form=publish_form,
        )


# ---------------------------------------------------------------------------
# Subscribe SQS to SNS
# ---------------------------------------------------------------------------

@sns_pages_blueprint.route("/sns/subscriptions/sqs", methods=["GET"])
def sns_subscribe_page():
    return redirect(url_for("sns_pages.sns", tab="subscribe"))


@sns_pages_blueprint.route("/sns/subscriptions/sqs", methods=["POST"])
def sns_subscribe_submit():
    topic_name = (request.form.get("topic_name") or "").strip()
    queue_name = (request.form.get("queue_name") or "").strip()

    subscribe_form = {"topic_name": topic_name, "queue_name": queue_name}

    if not topic_name or not queue_name:
        return render_sns_page(
            active_tab="subscribe",
            subscribe_result="Informe nome do topico e da fila para assinatura.",
            subscribe_form=subscribe_form,
        )

    topic_arn = get_topic_arn_by_name(topic_name)
    if not topic_arn:
        return render_sns_page(
            active_tab="subscribe",
            subscribe_result=f"Topico '{topic_name}' nao encontrado.",
            subscribe_form=subscribe_form,
        )

    queue_info = get_queue_info_by_name(queue_name)
    if not queue_info:
        return render_sns_page(
            active_tab="subscribe",
            subscribe_result=f"Fila '{queue_name}' nao encontrada.",
            subscribe_form=subscribe_form,
        )

    try:
        ensure_sqs_policy_for_sns_subscription(queue_info["url"], queue_info["arn"], topic_arn)
        sns_client = build_sns_client(topic_arn)
        response = sns_client.subscribe(TopicArn=topic_arn, Protocol="sqs", Endpoint=queue_info["arn"])
        subscription_arn = response.get("SubscriptionArn", "-")
        return render_sns_page(
            active_tab="subscribe",
            subscribe_result=f"Assinatura criada com sucesso. SubscriptionArn: {subscription_arn}",
            subscribe_form={},
        )
    except Exception as exc:
        return render_sns_page(
            active_tab="subscribe",
            subscribe_result=f"Erro ao criar assinatura SNS->SQS: {exc}",
            subscribe_form=subscribe_form,
        )

