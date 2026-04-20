#!/bin/sh

# Use awslocal (a wrapper for aws cli) if installed, or standard aws cli
alias awslocal='aws --endpoint-url=http://ministack:4566'

echo "Creating S3 bucket..."
awslocal s3 mb s3://my-test-bucket

echo "Creating SQS queue..."
awslocal sqs create-queue --queue-name my-local-queue

echo "Resources initialized."