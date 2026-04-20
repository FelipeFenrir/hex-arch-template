import json

from flask import Blueprint, redirect, render_template, request, url_for

from ..clients import build_sqs_client
from ...utils import parse_json_field

sqs_pages_blueprint = Blueprint("sqs_pages", __name__)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def render_sqs_page(
    active_tab: str = "monitor",
    create_result: str | None = None,
    send_result: str | None = None,
    create_form: dict | None = None,
    send_form: dict | None = None,
):
    return render_template(
        "monitor.html",
        active_tab=active_tab,
        create_result=create_result,
        send_result=send_result,
        create_form=create_form or {},
        send_form=send_form or {},
    )


# ---------------------------------------------------------------------------
# SQS monitor page
# ---------------------------------------------------------------------------

@sqs_pages_blueprint.route("/monitor")
def monitor():
    active_tab = (request.args.get("tab") or "monitor").strip().lower()
    if active_tab not in {"monitor", "create", "send"}:
        active_tab = "monitor"
    return render_sqs_page(active_tab=active_tab)


# ---------------------------------------------------------------------------
# Create queue
# ---------------------------------------------------------------------------

@sqs_pages_blueprint.route("/sqs/queues/create", methods=["GET"])
def create_queue_page():
    return redirect(url_for("sqs_pages.monitor", tab="create"))


@sqs_pages_blueprint.route("/sqs/queues/create", methods=["POST"])
def create_queue_submit():
    queue_name = (request.form.get("queue_name") or "").strip()
    is_fifo = bool(request.form.get("is_fifo"))
    content_based_dedup = bool(request.form.get("content_based_dedup"))

    create_form = {
        "queue_name": queue_name,
        "is_fifo": is_fifo,
        "content_based_dedup": content_based_dedup,
    }

    if not queue_name:
        return render_sqs_page(active_tab="create", create_result="Informe o nome da fila.", create_form=create_form)

    if is_fifo and not queue_name.endswith(".fifo"):
        return render_sqs_page(
            active_tab="create",
            create_result="Filas FIFO devem terminar com '.fifo'.",
            create_form=create_form,
        )

    attributes = {}
    if is_fifo:
        attributes["FifoQueue"] = "true"
        if content_based_dedup:
            attributes["ContentBasedDeduplication"] = "true"

    try:
        sqs = build_sqs_client()
        response = sqs.create_queue(QueueName=queue_name, Attributes=attributes) if attributes else sqs.create_queue(QueueName=queue_name)
        queue_url = response.get("QueueUrl", "-")
        return render_sqs_page(
            active_tab="create",
            create_result=f"Fila criada com sucesso: {queue_name} ({queue_url})",
            create_form={},
        )
    except Exception as exc:
        return render_sqs_page(
            active_tab="create",
            create_result=f"Erro ao criar fila: {exc}",
            create_form=create_form,
        )


# ---------------------------------------------------------------------------
# Send message
# ---------------------------------------------------------------------------

@sqs_pages_blueprint.route("/sqs/messages/send", methods=["GET"])
def send_message_page():
    return redirect(url_for("sqs_pages.monitor", tab="send"))


@sqs_pages_blueprint.route("/sqs/messages/send", methods=["POST"])
def send_message_submit():
    queue_name = (request.form.get("queue_name") or "").strip()
    headers_text = request.form.get("headers", "{}")
    body_text = request.form.get("body", "")
    batch_raw = request.form.get("batch", 1)

    try:
        batch_count = int(batch_raw)
    except (TypeError, ValueError):
        batch_count = 0

    send_form = {
        "queue_name": queue_name,
        "headers": headers_text,
        "body": body_text,
        "batch": batch_raw,
    }

    if not queue_name:
        return render_sqs_page(active_tab="send", send_result="Informe o nome da fila.", send_form=send_form)

    if batch_count < 1 or batch_count > 10:
        return render_sqs_page(active_tab="send", send_result="Batch deve ser entre 1 e 10.", send_form=send_form)

    try:
        headers_dict = parse_json_field(headers_text, "headers")
        body_dict = parse_json_field(body_text, "body")
    except ValueError as exc:
        return render_sqs_page(active_tab="send", send_result=str(exc), send_form=send_form)

    sqs = build_sqs_client()

    try:
        queue_url = sqs.get_queue_url(QueueName=queue_name)["QueueUrl"]
    except Exception as exc:
        return render_sqs_page(
            active_tab="send",
            send_result=f"Fila '{queue_name}' nao encontrada. Crie a fila antes de enviar mensagens. Detalhe: {exc}",
            send_form=send_form,
        )

    message_attributes = {
        key: {"StringValue": str(value), "DataType": "String"} for key, value in headers_dict.items()
    }

    entries = []
    for i in range(batch_count):
        entry = {"Id": str(i), "MessageBody": json.dumps(body_dict)}
        if message_attributes:
            entry["MessageAttributes"] = message_attributes
        entries.append(entry)

    try:
        sqs.send_message_batch(QueueUrl=queue_url, Entries=entries)
        return render_sqs_page(
            active_tab="send",
            send_result=f"{batch_count} mensagem(ns) enviada(s) para '{queue_name}'.",
            send_form={},
        )
    except Exception as exc:
        return render_sqs_page(
            active_tab="send",
            send_result=f"Erro ao enviar mensagens: {exc}",
            send_form=send_form,
        )

