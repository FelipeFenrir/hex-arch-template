import json

from .clients import build_sqs_client


def list_available_queues() -> list[dict[str, str]]:
    sqs = build_sqs_client()
    response = sqs.list_queues()
    queue_urls = response.get("QueueUrls", [])

    queues: list[dict[str, str]] = []
    for queue_url in queue_urls:
        queue_name = queue_url.rstrip("/").split("/")[-1]
        queues.append({"name": queue_name, "url": queue_url})

    return sorted(queues, key=lambda q: q["name"].lower())


def get_queue_info_by_name(queue_name: str) -> dict[str, str] | None:
    sqs = build_sqs_client()
    try:
        queue_url = sqs.get_queue_url(QueueName=queue_name)["QueueUrl"]
    except Exception:
        return None

    attrs = sqs.get_queue_attributes(QueueUrl=queue_url, AttributeNames=["QueueArn"]).get("Attributes", {})
    queue_arn = attrs.get("QueueArn")
    if not queue_arn:
        return None

    return {"url": queue_url, "arn": queue_arn}


def ensure_sqs_policy_for_sns_subscription(queue_url: str, queue_arn: str, topic_arn: str) -> None:
    sqs = build_sqs_client(queue_url)

    policy = {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Sid": "AllowSnsPublish",
                "Effect": "Allow",
                "Principal": {"Service": "sns.amazonaws.com"},
                "Action": "sqs:SendMessage",
                "Resource": queue_arn,
                "Condition": {"ArnEquals": {"aws:SourceArn": topic_arn}},
            }
        ],
    }

    sqs.set_queue_attributes(QueueUrl=queue_url, Attributes={"Policy": json.dumps(policy)})


def matches_message_header(message: dict, header_key: str, header_value: str) -> bool:
    attrs = message.get("MessageAttributes", {}) or {}
    key_map = {k.lower(): v for k, v in attrs.items()}

    if header_key not in key_map:
        return False

    if not header_value:
        return True

    candidate = key_map[header_key]
    if isinstance(candidate, dict):
        value = str(candidate.get("StringValue", "")).lower()
    else:
        value = str(candidate).lower()

    return header_value in value


def filter_messages(messages: list[dict], search: str, header_key: str, header_value: str) -> list[dict]:
    filtered = messages

    if search:
        filtered = [m for m in filtered if search in json.dumps(m).lower()]

    if header_key:
        filtered = [m for m in filtered if matches_message_header(m, header_key, header_value)]

    return filtered

