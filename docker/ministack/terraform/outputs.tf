output "s3_bucket_names" {
  description = "Managed S3 bucket names"
  value       = sort(keys(aws_s3_bucket.buckets))
}

output "sqs_queue_names" {
  description = "Managed SQS queue names"
  value       = sort(keys(aws_sqs_queue.queues))
}

output "sns_topic_names" {
  description = "Managed SNS topic names"
  value       = sort(keys(aws_sns_topic.topics))
}

output "notifications_subscription_arn" {
  description = "Subscription ARN for notifications topic to notifications queue"
  value       = aws_sns_topic_subscription.notifications_to_queue.arn
}
