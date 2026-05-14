locals {
  s3_buckets = [
    "my-test-bucket",
    "test-bucket-1",
    "test-bucket-2",
    "logs-archive",
  ]

  sqs_queues = [
    "my-local-queue",
    "task-queue",
    "notifications",
    "dlq-messages",
  ]

  sns_topics = [
    "order-events",
    "alerts",
    "notifications",
  ]
}

resource "aws_s3_bucket" "buckets" {
  for_each = toset(local.s3_buckets)

  bucket        = each.value
  force_destroy = true
}

resource "aws_sqs_queue" "queues" {
  for_each = toset(local.sqs_queues)

  name = each.value
}

resource "aws_sns_topic" "topics" {
  for_each = toset(local.sns_topics)

  name = each.value
}

data "aws_iam_policy_document" "allow_notifications_topic" {
  statement {
    sid    = "AllowSnsToSendNotifications"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["sns.amazonaws.com"]
    }

    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.queues["notifications"].arn]

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_sns_topic.topics["notifications"].arn]
    }
  }
}

resource "aws_sqs_queue_policy" "notifications" {
  queue_url = aws_sqs_queue.queues["notifications"].id
  policy    = data.aws_iam_policy_document.allow_notifications_topic.json
}

resource "aws_sns_topic_subscription" "notifications_to_queue" {
  topic_arn = aws_sns_topic.topics["notifications"].arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.queues["notifications"].arn

  raw_message_delivery = true

  depends_on = [aws_sqs_queue_policy.notifications]
}
