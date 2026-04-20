from flask import Blueprint, jsonify, request

from ..services import (
    collect_bucket_stats,
    create_bucket,
    delete_bucket,
    get_bucket_details,
    list_available_buckets,
    update_bucket_settings,
    validate_bucket_name,
)

s3_api_blueprint = Blueprint("s3_api", __name__)


# ---------------------------------------------------------------------------
# Bucket listing / details
# ---------------------------------------------------------------------------

@s3_api_blueprint.route("/s3/buckets", methods=["GET"])
def s3_buckets():
    try:
        return jsonify({"buckets": list_available_buckets()})
    except Exception as exc:
        return jsonify({"buckets": [], "error": str(exc)}), 500


@s3_api_blueprint.route("/s3/buckets", methods=["POST"])
def s3_create_bucket_api():
    body = request.json or {}
    bucket_name = str(body.get("bucketName") or "").strip()
    versioning_status = str(body.get("versioningStatus") or "Disabled").strip() or "Disabled"
    tags = body.get("tags") or {}

    validation_message = validate_bucket_name(bucket_name)
    if validation_message:
        return jsonify({"status": "invalid", "error": validation_message}), 400

    if not isinstance(tags, dict):
        return jsonify({"status": "invalid", "error": "tags deve ser um objeto JSON."}), 400

    try:
        result = create_bucket(bucket_name, versioning_status=versioning_status, tags=tags)
        return jsonify(
            {
                "status": "created",
                "bucketName": bucket_name,
                "location": result.get("location", "-"),
            }
        )
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@s3_api_blueprint.route("/s3/buckets/details", methods=["GET"])
def s3_bucket_details():
    bucket_name = (request.args.get("name") or "").strip()
    validation_message = validate_bucket_name(bucket_name)
    if validation_message:
        return jsonify({"error": validation_message}), 400

    try:
        details = get_bucket_details(bucket_name)
        return jsonify(details)
    except Exception as exc:
        return jsonify({"error": str(exc)}), 500


@s3_api_blueprint.route("/s3/buckets/stats", methods=["GET"])
def s3_bucket_stats():
    bucket_name = (request.args.get("name") or "").strip()
    validation_message = validate_bucket_name(bucket_name)
    if validation_message:
        return jsonify({"error": validation_message}), 400

    try:
        return jsonify(collect_bucket_stats(bucket_name))
    except Exception as exc:
        return jsonify({"error": str(exc)}), 500


# ---------------------------------------------------------------------------
# Bucket settings / deletion
# ---------------------------------------------------------------------------

def _s3_update_settings_impl():
    body = request.json or {}
    bucket_name = str(body.get("bucketName") or "").strip()
    versioning_status = str(body.get("versioningStatus") or "Disabled").strip() or "Disabled"
    tags = body.get("tags") or {}

    validation_message = validate_bucket_name(bucket_name)
    if validation_message:
        return jsonify({"status": "invalid", "error": validation_message}), 400

    if not isinstance(tags, dict):
        return jsonify({"status": "invalid", "error": "tags deve ser um objeto JSON."}), 400

    try:
        update_bucket_settings(bucket_name, versioning_status=versioning_status, tags=tags)
        return jsonify({"status": "updated"})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@s3_api_blueprint.route("/s3/buckets", methods=["PUT"])
def s3_update_bucket_api():
    return _s3_update_settings_impl()


@s3_api_blueprint.route("/s3/buckets/settings", methods=["PUT"])
def s3_update_settings_api():
    # Legacy alias to keep existing clients working while moving to REST-like CRUD.
    return _s3_update_settings_impl()


@s3_api_blueprint.route("/s3/buckets", methods=["DELETE"])
def s3_delete_bucket_api():
    body = request.json or {}
    bucket_name = str(body.get("bucketName") or "").strip()
    force_delete = bool(body.get("forceDelete"))

    validation_message = validate_bucket_name(bucket_name)
    if validation_message:
        return jsonify({"status": "invalid", "error": validation_message}), 400

    try:
        delete_bucket(bucket_name, force_delete=force_delete)
        return jsonify({"status": "deleted"})
    except ValueError as exc:
        return jsonify({"status": "blocked", "error": str(exc)}), 409
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500

