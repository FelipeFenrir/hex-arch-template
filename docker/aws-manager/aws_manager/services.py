# Deprecated: services split into aws_manager.sqs.services and aws_manager.sns.services.
# Re-exported here for backward compatibility.

from aws_manager.sqs.services import (  # noqa: F401
    ensure_sqs_policy_for_sns_subscription,
    filter_messages,
    get_queue_info_by_name,
    list_available_queues,
    matches_message_header,
)
from aws_manager.sns.services import (  # noqa: F401
    get_topic_arn_by_name,
    list_available_topics,
    list_topic_subscriptions,
)
