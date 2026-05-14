from decimal import Decimal

from boto3.dynamodb.types import TypeDeserializer, TypeSerializer

from .clients import build_dynamodb_client, build_dynamodb_resource

_serializer = TypeSerializer()
_deserializer = TypeDeserializer()


def _python_to_ddb_map(data: dict) -> dict:
    return {key: _serializer.serialize(value) for key, value in data.items()}


def _ddb_to_python_map(data: dict) -> dict:
    return {key: _deserializer.deserialize(value) for key, value in data.items()}


def _json_safe(value):
    if isinstance(value, Decimal):
        if value % 1 == 0:
            return int(value)
        return float(value)
    if isinstance(value, dict):
        return {k: _json_safe(v) for k, v in value.items()}
    if isinstance(value, list):
        return [_json_safe(v) for v in value]
    return value


def _extract_billing_mode(table_desc: dict) -> str:
    summary = table_desc.get("BillingModeSummary") or {}
    return str(summary.get("BillingMode") or "PROVISIONED")


def list_available_tables() -> list[dict]:
    dynamodb = build_dynamodb_client()
    table_names = dynamodb.list_tables().get("TableNames", [])

    tables: list[dict] = []
    for table_name in table_names:
        summary = get_table_summary(table_name)
        if summary:
            tables.append(summary)

    return sorted(tables, key=lambda table: str(table["name"]).lower())


def get_table_summary(table_name: str) -> dict | None:
    dynamodb = build_dynamodb_client()
    try:
        table_desc = dynamodb.describe_table(TableName=table_name).get("Table", {})
    except Exception:
        return None

    throughput = table_desc.get("ProvisionedThroughput") or {}

    return {
        "name": table_name,
        "status": table_desc.get("TableStatus", "UNKNOWN"),
        "itemCount": int(table_desc.get("ItemCount", 0)),
        "billingMode": _extract_billing_mode(table_desc),
        "hashKey": next((k.get("AttributeName") for k in table_desc.get("KeySchema", []) if k.get("KeyType") == "HASH"), ""),
        "readCapacity": int(throughput.get("ReadCapacityUnits", 0) or 0),
        "writeCapacity": int(throughput.get("WriteCapacityUnits", 0) or 0),
    }


def create_table(
    table_name: str,
    hash_key: str,
    billing_mode: str = "PAY_PER_REQUEST",
    read_capacity: int = 5,
    write_capacity: int = 5,
) -> dict:
    dynamodb = build_dynamodb_client()
    normalized_billing = billing_mode.strip().upper()

    payload = {
        "TableName": table_name,
        "AttributeDefinitions": [{"AttributeName": hash_key, "AttributeType": "S"}],
        "KeySchema": [{"AttributeName": hash_key, "KeyType": "HASH"}],
    }

    if normalized_billing == "PAY_PER_REQUEST":
        payload["BillingMode"] = "PAY_PER_REQUEST"
    else:
        payload["BillingMode"] = "PROVISIONED"
        payload["ProvisionedThroughput"] = {
            "ReadCapacityUnits": max(1, int(read_capacity)),
            "WriteCapacityUnits": max(1, int(write_capacity)),
        }

    dynamodb.create_table(**payload)
    summary = get_table_summary(table_name)
    return summary or {
        "name": table_name,
        "status": "CREATING",
        "itemCount": 0,
        "billingMode": normalized_billing,
        "hashKey": hash_key,
        "readCapacity": max(1, int(read_capacity)) if normalized_billing == "PROVISIONED" else 0,
        "writeCapacity": max(1, int(write_capacity)) if normalized_billing == "PROVISIONED" else 0,
    }


def update_table_settings(
    table_name: str,
    billing_mode: str,
    read_capacity: int | None = None,
    write_capacity: int | None = None,
) -> dict | None:
    dynamodb = build_dynamodb_client()
    normalized_billing = billing_mode.strip().upper()

    if normalized_billing == "PAY_PER_REQUEST":
        dynamodb.update_table(TableName=table_name, BillingMode="PAY_PER_REQUEST")
    else:
        dynamodb.update_table(
            TableName=table_name,
            BillingMode="PROVISIONED",
            ProvisionedThroughput={
                "ReadCapacityUnits": max(1, int(read_capacity or 5)),
                "WriteCapacityUnits": max(1, int(write_capacity or 5)),
            },
        )

    return get_table_summary(table_name)


def _scan_table_keys(table_name: str, hash_key: str) -> list[dict]:
    resource = build_dynamodb_resource()
    table = resource.Table(table_name)

    keys: list[dict] = []
    last_evaluated_key = None
    expr_attr_names = {"#hk": hash_key}

    while True:
        kwargs = {
            "ProjectionExpression": "#hk",
            "ExpressionAttributeNames": expr_attr_names,
        }
        if last_evaluated_key:
            kwargs["ExclusiveStartKey"] = last_evaluated_key

        response = table.scan(**kwargs)
        keys.extend([{hash_key: item[hash_key]} for item in response.get("Items", []) if hash_key in item])
        last_evaluated_key = response.get("LastEvaluatedKey")
        if not last_evaluated_key:
            break

    return keys


def delete_table(table_name: str, force_delete: bool = False) -> None:
    summary = get_table_summary(table_name)
    if not summary:
        raise ValueError(f"Tabela '{table_name}' nao encontrada")

    item_count = int(summary.get("itemCount", 0))
    if item_count > 0 and not force_delete:
        raise ValueError("Tabela possui itens. Confirme exclusao forcada para remover tudo.")

    if item_count > 0 and force_delete:
        hash_key = str(summary.get("hashKey") or "")
        if hash_key:
            resource = build_dynamodb_resource()
            table = resource.Table(table_name)
            keys = _scan_table_keys(table_name, hash_key)
            with table.batch_writer() as batch:
                for key in keys:
                    batch.delete_item(Key=key)

    dynamodb = build_dynamodb_client()
    dynamodb.delete_table(TableName=table_name)


def scan_table_items(table_name: str, limit: int = 25) -> list[dict]:
    dynamodb = build_dynamodb_client()
    response = dynamodb.scan(TableName=table_name, Limit=max(1, min(int(limit), 100)))
    items = response.get("Items", [])
    return [_json_safe(_ddb_to_python_map(item)) for item in items]


def query_table_items_by_key(table_name: str, key: dict, limit: int = 25) -> list[dict]:
    if not isinstance(key, dict) or not key:
        raise ValueError("key must be a non-empty JSON object")

    dynamodb = build_dynamodb_client()
    ddb_key = _python_to_ddb_map(key)
    normalized_limit = max(1, min(int(limit), 100))

    # One key field means hash-key query; multiple fields are treated as full get_item key.
    if len(key) == 1:
        key_name = next(iter(key.keys()))
        response = dynamodb.query(
            TableName=table_name,
            KeyConditionExpression="#k = :v",
            ExpressionAttributeNames={"#k": key_name},
            ExpressionAttributeValues={":v": ddb_key[key_name]},
            Limit=normalized_limit,
        )
        items = response.get("Items", [])
        return [_json_safe(_ddb_to_python_map(item)) for item in items]

    response = dynamodb.get_item(TableName=table_name, Key=ddb_key)
    item = response.get("Item")
    if not item:
        return []
    return [_json_safe(_ddb_to_python_map(item))]


def put_table_item(table_name: str, item: dict) -> None:
    dynamodb = build_dynamodb_client()
    dynamodb.put_item(TableName=table_name, Item=_python_to_ddb_map(item))


def delete_table_item(table_name: str, key: dict) -> None:
    dynamodb = build_dynamodb_client()
    dynamodb.delete_item(TableName=table_name, Key=_python_to_ddb_map(key))


