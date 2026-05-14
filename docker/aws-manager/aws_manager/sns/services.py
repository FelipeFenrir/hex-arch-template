from .clients import build_sns_client


def list_available_topics() -> list[dict[str, str]]:
    sns = build_sns_client()
    response = sns.list_topics()
    topic_arns = [t.get("TopicArn") for t in response.get("Topics", []) if t.get("TopicArn")]

    topics: list[dict[str, str]] = []
    for topic_arn in topic_arns:
        topic_name = topic_arn.rsplit(":", 1)[-1]
        topics.append({"name": topic_name, "arn": topic_arn})

    return sorted(topics, key=lambda t: t["name"].lower())


def get_topic_arn_by_name(topic_name: str) -> str | None:
    for topic in list_available_topics():
        if topic["name"] == topic_name:
            return topic["arn"]
    return None


def list_topic_subscriptions(topic_arn: str) -> list[dict[str, str]]:
    sns = build_sns_client(topic_arn)
    paginator = sns.get_paginator("list_subscriptions_by_topic")

    subscriptions: list[dict[str, str]] = []
    for page in paginator.paginate(TopicArn=topic_arn):
        for sub in page.get("Subscriptions", []):
            subscriptions.append(
                {
                    "subscriptionArn": sub.get("SubscriptionArn", "-"),
                    "protocol": sub.get("Protocol", "-"),
                    "endpoint": sub.get("Endpoint", "-"),
                }
            )

    return subscriptions


def get_topic_runtime_summary(topic_name: str) -> dict[str, str | int | bool] | None:
    topic_arn = get_topic_arn_by_name(topic_name)
    if not topic_arn:
        return None

    sns = build_sns_client(topic_arn)
    attrs = sns.get_topic_attributes(TopicArn=topic_arn).get("Attributes", {})
    subscriptions = list_topic_subscriptions(topic_arn)

    return {
        "name": topic_name,
        "arn": topic_arn,
        "displayName": attrs.get("DisplayName", ""),
        "isFifo": topic_name.endswith(".fifo") or attrs.get("FifoTopic") == "true",
        "subscriptionsCount": len(subscriptions),
        "hasActiveSubscriptions": len(subscriptions) > 0,
    }


def update_topic_runtime_attributes(topic_name: str, display_name: str | None = None) -> dict[str, str | int | bool] | None:
    topic_arn = get_topic_arn_by_name(topic_name)
    if not topic_arn:
        return None

    sns = build_sns_client(topic_arn)
    if display_name is not None:
        sns.set_topic_attributes(TopicArn=topic_arn, AttributeName="DisplayName", AttributeValue=str(display_name).strip())

    return get_topic_runtime_summary(topic_name)


