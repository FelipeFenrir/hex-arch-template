from flask import Blueprint, jsonify, request

from ..services import (
    create_table,
    delete_table,
    get_table_summary,
    list_available_tables,
    put_table_item,
    query_table_items_by_key,
    scan_table_items,
    delete_table_item,
    update_table_settings,
)

dynamodb_api_blueprint = Blueprint("dynamodb_api", __name__)


@dynamodb_api_blueprint.route("/api/dynamodb/tables", methods=["GET"])
def dynamodb_tables_api():
    try:
        return jsonify({"tables": list_available_tables()})
    except Exception as exc:
        return jsonify({"tables": [], "error": str(exc)}), 500


@dynamodb_api_blueprint.route("/api/dynamodb/tables", methods=["POST"])
def dynamodb_create_table_api():
    body = request.json or {}
    table_name = str(body.get("tableName") or "").strip()
    hash_key = str(body.get("hashKey") or "").strip()
    billing_mode = str(body.get("billingMode") or "PAY_PER_REQUEST").strip() or "PAY_PER_REQUEST"
    read_capacity = body.get("readCapacity")
    write_capacity = body.get("writeCapacity")

    if not table_name:
        return jsonify({"status": "invalid", "error": "tableName is required"}), 400
    if not hash_key:
        return jsonify({"status": "invalid", "error": "hashKey is required"}), 400

    try:
        table = create_table(
            table_name=table_name,
            hash_key=hash_key,
            billing_mode=billing_mode,
            read_capacity=int(read_capacity or 5),
            write_capacity=int(write_capacity or 5),
        )
        return jsonify({"status": "created", "table": table})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@dynamodb_api_blueprint.route("/api/dynamodb/tables/<table_name>/summary", methods=["GET"])
def dynamodb_table_summary_api(table_name: str):
    try:
        summary = get_table_summary(table_name)
        if not summary:
            return jsonify({"status": "error", "error": f"Tabela '{table_name}' nao encontrada"}), 404
        return jsonify(summary)
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@dynamodb_api_blueprint.route("/api/dynamodb/tables/<table_name>", methods=["PUT"])
def dynamodb_update_table_api(table_name: str):
    body = request.json or {}
    billing_mode = str(body.get("billingMode") or "PAY_PER_REQUEST").strip() or "PAY_PER_REQUEST"
    read_capacity = body.get("readCapacity")
    write_capacity = body.get("writeCapacity")

    try:
        table = update_table_settings(
            table_name=table_name,
            billing_mode=billing_mode,
            read_capacity=int(read_capacity) if read_capacity is not None else None,
            write_capacity=int(write_capacity) if write_capacity is not None else None,
        )
        if not table:
            return jsonify({"status": "error", "error": f"Tabela '{table_name}' nao encontrada"}), 404
        return jsonify({"status": "updated", "table": table})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@dynamodb_api_blueprint.route("/api/dynamodb/tables/<table_name>", methods=["DELETE"])
def dynamodb_delete_table_api(table_name: str):
    force = str(request.args.get("force") or "false").strip().lower() in {"1", "true", "yes", "y"}

    try:
        summary = get_table_summary(table_name)
        if not summary:
            return jsonify({"status": "error", "error": f"Tabela '{table_name}' nao encontrada"}), 404

        if int(summary.get("itemCount", 0)) > 0 and not force:
            return (
                jsonify(
                    {
                        "status": "blocked",
                        "error": "Tabela possui itens. Confirme exclusao forcada.",
                        "requiresConfirmation": True,
                        "table": summary,
                    }
                ),
                409,
            )

        delete_table(table_name, force_delete=force)
        return jsonify({"status": "deleted", "tableName": table_name})
    except ValueError as exc:
        return jsonify({"status": "blocked", "error": str(exc)}), 409
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@dynamodb_api_blueprint.route("/api/dynamodb/tables/<table_name>/items", methods=["GET"])
def dynamodb_scan_items_api(table_name: str):
    limit = int(request.args.get("limit", 25))

    try:
        return jsonify({"items": scan_table_items(table_name, limit=limit)})
    except Exception as exc:
        return jsonify({"items": [], "error": str(exc)}), 500


@dynamodb_api_blueprint.route("/api/dynamodb/tables/<table_name>/items/query", methods=["POST"])
def dynamodb_query_items_api(table_name: str):
    body = request.json or {}
    key = body.get("key")
    limit = body.get("limit", 25)

    if not isinstance(key, dict) or not key:
        return jsonify({"status": "invalid", "error": "key must be a non-empty JSON object"}), 400

    try:
        items = query_table_items_by_key(table_name, key, int(limit))
        return jsonify({"items": items})
    except ValueError as exc:
        return jsonify({"status": "invalid", "error": str(exc)}), 400
    except Exception as exc:
        return jsonify({"items": [], "error": str(exc)}), 500


@dynamodb_api_blueprint.route("/api/dynamodb/tables/<table_name>/items", methods=["POST"])
def dynamodb_put_item_api(table_name: str):
    body = request.json or {}
    item = body.get("item")

    if not isinstance(item, dict):
        return jsonify({"status": "invalid", "error": "item must be a JSON object"}), 400

    try:
        put_table_item(table_name, item)
        return jsonify({"status": "upserted"})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500


@dynamodb_api_blueprint.route("/api/dynamodb/tables/<table_name>/items", methods=["DELETE"])
def dynamodb_delete_item_api(table_name: str):
    body = request.json or {}
    key = body.get("key")

    if not isinstance(key, dict):
        return jsonify({"status": "invalid", "error": "key must be a JSON object"}), 400

    try:
        delete_table_item(table_name, key)
        return jsonify({"status": "deleted"})
    except Exception as exc:
        return jsonify({"status": "error", "error": str(exc)}), 500

