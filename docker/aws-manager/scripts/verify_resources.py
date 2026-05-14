#!/usr/bin/env python3
"""
Complete verification of AWS Manager with MiniStack resources.
Tests S3, SQS, SNS without needing Flask server running.
"""

from aws_manager.clients import _build_client
from aws_manager.registry import SERVICE_REGISTRY
import os

os.environ['AWS_ACCESS_KEY_ID'] = 'test'
os.environ['AWS_SECRET_ACCESS_KEY'] = 'test'

endpoint = "http://127.0.0.1:4566"
region = "us-east-1"

print("=" * 70)
print("AWS MANAGER RESOURCE VERIFICATION")
print("=" * 70)

services_to_test = ['s3', 'sqs', 'sns']

for service in services_to_test:
    print(f"\n[{service.upper()}]")
    entries = SERVICE_REGISTRY.get(service, [])

    for resource_type, boto3_service, method_name, response_key in entries:
        try:
            client = _build_client(boto3_service, endpoint)
            method = getattr(client, method_name)
            kwargs = {}
            resp = method(**kwargs)
            items = resp.get(response_key, [])

            if isinstance(items, dict) and "Items" in items:
                items = items.get("Items", [])

            count = len(items) if isinstance(items, list) else 0
            print(f"  {resource_type}: {count} resources", end="")

            if count > 0 and isinstance(items, list):
                for i, item in enumerate(items[:3]):
                    if isinstance(item, dict):
                        name = item.get('Name') or item.get('TopicArn') or item.get('id') or str(item)
                        print(f"\n    - {name}", end="")
                if count > 3:
                    print(f"\n    ... and {count - 3} more", end="")
            print()

        except Exception as e:
            print(f"  {resource_type}: ERROR - {e}")

print("\n" + "=" * 70)
print("VERIFICATION COMPLETE")
print("=" * 70)

