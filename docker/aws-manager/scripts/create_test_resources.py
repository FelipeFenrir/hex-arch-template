#!/usr/bin/env python3
"""Create test resources in MiniStack for testing AWS Manager."""

import boto3
import sys
import os

# Set dummy AWS credentials for MiniStack/LocalStack
os.environ.setdefault('AWS_ACCESS_KEY_ID', 'test')
os.environ.setdefault('AWS_SECRET_ACCESS_KEY', 'test')

# Configure boto3 to use MiniStack
s3 = boto3.client('s3', endpoint_url='http://localhost:4566', region_name='us-east-1')
sqs = boto3.client('sqs', endpoint_url='http://localhost:4566', region_name='us-east-1')
sns = boto3.client('sns', endpoint_url='http://localhost:4566', region_name='us-east-1')

def create_s3_buckets():
    """Create test S3 buckets."""
    buckets = ['test-bucket-1', 'test-bucket-2', 'logs-archive']
    for bucket_name in buckets:
        try:
            s3.create_bucket(Bucket=bucket_name)
            print(f"✓ Created S3 bucket: {bucket_name}")
        except Exception as e:
            if 'BucketAlreadyOwnedByYou' in str(e):
                print(f"→ S3 bucket already exists: {bucket_name}")
            else:
                print(f"✗ Error creating bucket {bucket_name}: {e}")

def create_sqs_queues():
    """Create test SQS queues."""
    queues = ['task-queue', 'notifications', 'dlq-messages']
    for queue_name in queues:
        try:
            response = sqs.create_queue(QueueName=queue_name)
            print(f"✓ Created SQS queue: {queue_name}")
        except Exception as e:
            if 'QueueAlreadyExists' in str(e) or 'already exists' in str(e):
                print(f"→ SQS queue already exists: {queue_name}")
            else:
                print(f"✗ Error creating queue {queue_name}: {e}")

def create_sns_topics():
    """Create test SNS topics."""
    topics = ['order-events', 'alerts', 'notifications']
    for topic_name in topics:
        try:
            response = sns.create_topic(Name=topic_name)
            print(f"✓ Created SNS topic: {topic_name}")
        except Exception as e:
            print(f"✗ Error creating topic {topic_name}: {e}")

def list_resources():
    """List all created resources."""
    print("\n" + "="*50)
    print("Current Resources in MiniStack:")
    print("="*50)

    # List S3 buckets
    print("\n📦 S3 Buckets:")
    try:
        response = s3.list_buckets()
        if response.get('Buckets'):
            for bucket in response['Buckets']:
                print(f"  - {bucket['Name']}")
        else:
            print("  (none)")
    except Exception as e:
        print(f"  Error listing buckets: {e}")

    # List SQS queues
    print("\n📨 SQS Queues:")
    try:
        response = sqs.list_queues()
        if response.get('QueueUrls'):
            for url in response['QueueUrls']:
                queue_name = url.split('/')[-1]
                print(f"  - {queue_name}")
        else:
            print("  (none)")
    except Exception as e:
        print(f"  Error listing queues: {e}")

    # List SNS topics
    print("\n🔔 SNS Topics:")
    try:
        response = sns.list_topics()
        if response.get('Topics'):
            for topic in response['Topics']:
                topic_name = topic['TopicArn'].split(':')[-1]
                print(f"  - {topic_name}")
        else:
            print("  (none)")
    except Exception as e:
        print(f"  Error listing topics: {e}")

if __name__ == '__main__':
    print("Creating test resources in MiniStack...")
    print("="*50)

    try:
        create_s3_buckets()
        create_sqs_queues()
        create_sns_topics()
        list_resources()
        print("\n✓ Done!")
    except Exception as e:
        print(f"\n✗ Fatal error: {e}")
        sys.exit(1)


