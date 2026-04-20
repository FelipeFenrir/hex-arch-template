from flask import Blueprint, redirect, render_template, request, url_for

from ..services import create_bucket, update_bucket_settings, validate_bucket_name
from ...utils import parse_json_field

s3_pages_blueprint = Blueprint("s3_pages", __name__)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def render_s3_page(
    active_tab: str = "monitor",
    create_result: str | None = None,
    update_result: str | None = None,
    create_form: dict | None = None,
    update_form: dict | None = None,
):
    return render_template(
        "s3.html",
        active_tab=active_tab,
        create_result=create_result,
        update_result=update_result,
        create_form=create_form or {},
        update_form=update_form or {},
    )


# ---------------------------------------------------------------------------
# S3 workspace page
# ---------------------------------------------------------------------------

@s3_pages_blueprint.route("/s3")
def s3_workspace():
    active_tab = (request.args.get("tab") or "monitor").strip().lower()
    if active_tab not in {"monitor", "create", "settings"}:
        active_tab = "monitor"
    return render_s3_page(active_tab=active_tab)


# ---------------------------------------------------------------------------
# Create bucket
# ---------------------------------------------------------------------------

@s3_pages_blueprint.route("/s3/buckets/create", methods=["GET"])
def s3_create_bucket_page():
    return redirect(url_for("s3_pages.s3_workspace", tab="create"))


@s3_pages_blueprint.route("/s3/buckets/create", methods=["POST"])
def s3_create_bucket_submit():
    bucket_name = (request.form.get("bucket_name") or "").strip()
    versioning_status = (request.form.get("versioning_status") or "Disabled").strip() or "Disabled"
    tags_text = request.form.get("tags_json", "{}")

    create_form = {
        "bucket_name": bucket_name,
        "versioning_status": versioning_status,
        "tags_json": tags_text,
    }

    validation_message = validate_bucket_name(bucket_name)
    if validation_message:
        return render_s3_page(active_tab="create", create_result=validation_message, create_form=create_form)

    try:
        tags_dict = parse_json_field(tags_text, "tags_json")
    except ValueError as exc:
        return render_s3_page(active_tab="create", create_result=str(exc), create_form=create_form)

    if not isinstance(tags_dict, dict):
        return render_s3_page(
            active_tab="create",
            create_result="tags_json deve ser um objeto JSON de chave/valor.",
            create_form=create_form,
        )

    try:
        response = create_bucket(bucket_name, versioning_status=versioning_status, tags=tags_dict)
        location = response.get("location", "-")
        return render_s3_page(
            active_tab="create",
            create_result=f"Bucket criado com sucesso: {bucket_name} ({location})",
            create_form={},
        )
    except Exception as exc:
        return render_s3_page(
            active_tab="create",
            create_result=f"Erro ao criar bucket: {exc}",
            create_form=create_form,
        )


# ---------------------------------------------------------------------------
# Update bucket settings
# ---------------------------------------------------------------------------

@s3_pages_blueprint.route("/s3/buckets/update", methods=["POST"])
def s3_update_bucket_submit():
    bucket_name = (request.form.get("bucket_name") or "").strip()
    versioning_status = (request.form.get("versioning_status") or "Disabled").strip() or "Disabled"
    tags_text = request.form.get("tags_json", "{}")

    update_form = {
        "bucket_name": bucket_name,
        "versioning_status": versioning_status,
        "tags_json": tags_text,
    }

    validation_message = validate_bucket_name(bucket_name)
    if validation_message:
        return render_s3_page(active_tab="settings", update_result=validation_message, update_form=update_form)

    try:
        tags_dict = parse_json_field(tags_text, "tags_json")
    except ValueError as exc:
        return render_s3_page(active_tab="settings", update_result=str(exc), update_form=update_form)

    if not isinstance(tags_dict, dict):
        return render_s3_page(
            active_tab="settings",
            update_result="tags_json deve ser um objeto JSON de chave/valor.",
            update_form=update_form,
        )

    try:
        update_bucket_settings(bucket_name, versioning_status=versioning_status, tags=tags_dict)
        return render_s3_page(
            active_tab="settings",
            update_result=f"Configuracoes atualizadas com sucesso para o bucket '{bucket_name}'.",
            update_form=update_form,
        )
    except Exception as exc:
        return render_s3_page(
            active_tab="settings",
            update_result=f"Erro ao atualizar configuracoes do bucket: {exc}",
            update_form=update_form,
        )

