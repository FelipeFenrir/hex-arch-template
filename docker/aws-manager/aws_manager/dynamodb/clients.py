import os

import boto3

from ..clients import _resolve_endpoint
from ..config import DEFAULT_AWS_REGION


def build_dynamodb_client(url: str | None = None):
    endpoint_url = _resolve_endpoint(url)
    aws_access_key = os.getenv("AWS_ACCESS_KEY_ID", "test")
    aws_secret_key = os.getenv("AWS_SECRET_ACCESS_KEY", "test")

    return boto3.client(
        "dynamodb",
        region_name=os.getenv("AWS_DEFAULT_REGION", DEFAULT_AWS_REGION),
        endpoint_url=endpoint_url,
        aws_access_key_id=aws_access_key,
        aws_secret_access_key=aws_secret_key,
    )


def build_dynamodb_resource(url: str | None = None):
    endpoint_url = _resolve_endpoint(url)
    aws_access_key = os.getenv("AWS_ACCESS_KEY_ID", "test")
    aws_secret_key = os.getenv("AWS_SECRET_ACCESS_KEY", "test")

    return boto3.resource(
        "dynamodb",
        region_name=os.getenv("AWS_DEFAULT_REGION", DEFAULT_AWS_REGION),
        endpoint_url=endpoint_url,
        aws_access_key_id=aws_access_key,
        aws_secret_access_key=aws_secret_key,
    )

