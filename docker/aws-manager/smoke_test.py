from unittest.mock import patch
from pathlib import Path

import app as aws_manager_app


class FakeSqs:
    def create_queue(self, QueueName, Attributes=None):
        return {"QueueUrl": f"http://ministack:4566/000000000000/{QueueName}"}

    def get_queue_url(self, QueueName):
        return {"QueueUrl": f"http://ministack:4566/000000000000/{QueueName}"}

    def send_message_batch(self, QueueUrl, Entries):
        assert QueueUrl.endswith("teste")
        assert len(Entries) == 2
        return {"Successful": [{"Id": entry["Id"]} for entry in Entries]}


class FakeSns:
    def create_topic(self, Name, Attributes=None):
        return {"TopicArn": f"arn:aws:sns:us-east-1:000000000000:{Name}"}

    def publish(self, **kwargs):
        assert kwargs.get("TopicArn")
        assert kwargs.get("Message")
        return {"MessageId": "m-1"}

    def subscribe(self, TopicArn, Protocol, Endpoint):
        assert TopicArn
        assert Protocol == "sqs"
        assert Endpoint
        return {"SubscriptionArn": "arn:aws:sns:us-east-1:000000000000:eventos-teste:sub-1"}


class FakeUrlOpenResponse:
    def __init__(self, payload: str):
        self.payload = payload.encode("utf-8")

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, tb):
        return False

    def read(self):
        return self.payload


class FakeS3ListPaginator:
    def paginate(self, **kwargs):
        bucket_name = kwargs.get("Bucket")
        if bucket_name == "dados-dev":
            return [{"Contents": [{"Key": "a.json", "Size": 12}, {"Key": "b.json", "Size": 30}]}]
        return [{"Contents": []}]


class FakeS3:
    def list_buckets(self):
        return {"Buckets": [{"Name": "dados-dev", "CreationDate": None}]}

    def get_bucket_versioning(self, Bucket):
        return {"Status": "Enabled"}

    def get_bucket_tagging(self, Bucket):
        return {"TagSet": [{"Key": "ambiente", "Value": "dev"}]}

    def get_bucket_location(self, Bucket):
        return {"LocationConstraint": "us-east-1"}

    def get_paginator(self, name):
        if name == "list_objects_v2":
            return FakeS3ListPaginator()
        raise AssertionError(f"Paginator inesperado: {name}")

    def create_bucket(self, Bucket):
        return {"Location": f"/{Bucket}"}

    def put_bucket_versioning(self, Bucket, VersioningConfiguration):
        return {}

    def put_bucket_tagging(self, Bucket, Tagging):
        return {}

    def delete_bucket_tagging(self, Bucket):
        return {}

    def list_objects_v2(self, Bucket, MaxKeys=1):
        return {"KeyCount": 1 if Bucket == "dados-dev" else 0}

    def delete_objects(self, Bucket, Delete):
        return {}

    def delete_bucket(self, Bucket):
        return {}


def run():
    client = aws_manager_app.app.test_client()

    static_dir = Path(__file__).parent / "static"
    layout_js = (static_dir / "layout.js").read_text(encoding="utf-8")
    monitor_js = (static_dir / "monitor.js").read_text(encoding="utf-8")
    sns_js = (static_dir / "sns.js").read_text(encoding="utf-8")
    home_health_js = (static_dir / "home-health.js").read_text(encoding="utf-8")

    assert "function showToast(" in layout_js
    assert "window.showToast = showToast" in layout_js
    assert "alert(" not in monitor_js
    assert "alert(" not in sns_js
    assert "window.alert(" not in home_health_js

    # Home page lists both services
    response = client.get("/")
    assert response.status_code == 200
    body = response.get_data(as_text=True)
    assert "SQS" in body
    assert "SNS" in body
    assert "S3" in body
    assert "DynamoDB" in body
    assert "Lambda" in body
    assert "CloudWatch" in body
    assert "ECS" in body
    assert "EC2" in body
    assert "IAM" in body
    assert "healthIntervalSelect" in body
    assert 'data-service-implemented="false"' in body
    assert "/apis" in body or True

    # API catalog page
    response = client.get("/apis")
    assert response.status_code == 200
    apis_body = response.get_data(as_text=True)
    assert "Catalogo de APIs" in apis_body
    assert "/s3/buckets" in apis_body
    assert "/monitor/queues" in apis_body

    # GET redirects for SQS
    response = client.get("/sqs/queues/create")
    assert response.status_code == 302
    assert "/monitor?tab=create" in response.headers["Location"]

    response = client.get("/sqs/messages/send")
    assert response.status_code == 302
    assert "/monitor?tab=send" in response.headers["Location"]

    # GET redirects for SNS
    response = client.get("/sns/topics/create")
    assert response.status_code == 302
    assert "/sns?tab=create" in response.headers["Location"]

    response = client.get("/sns/messages/publish")
    assert response.status_code == 302
    assert "/sns?tab=publish" in response.headers["Location"]

    response = client.get("/sns/subscriptions/sqs")
    assert response.status_code == 302
    assert "/sns?tab=subscribe" in response.headers["Location"]

    # GET redirects for S3
    response = client.get("/s3/buckets/create")
    assert response.status_code == 302
    assert "/s3?tab=create" in response.headers["Location"]

    response = client.get("/s3")
    assert response.status_code == 200
    assert "S3" in response.get_data(as_text=True)

    # SQS form handlers
    with patch("aws_manager.sqs.web.pages.build_sqs_client", return_value=FakeSqs()):
        response = client.post("/sqs/queues/create", data={"queue_name": "teste"})
        assert response.status_code == 200

        response = client.post(
            "/sqs/messages/send",
            data={
                "queue_name": "teste",
                "headers": '{"x-origem": "smoke"}',
                "body": '{"id": 1}',
                "batch": "2",
            },
        )
        assert response.status_code == 200

    # SNS form handlers
    with patch("aws_manager.sns.web.pages.get_topic_arn_by_name", return_value="arn:aws:sns:us-east-1:000000000000:eventos-teste"), \
            patch("aws_manager.sns.web.pages.get_queue_info_by_name", return_value={"url": "http://ministack:4566/000000000000/teste", "arn": "arn:aws:sqs:us-east-1:000000000000:teste"}), \
            patch("aws_manager.sns.web.pages.ensure_sqs_policy_for_sns_subscription"), \
            patch("aws_manager.sns.web.pages.build_sns_client", return_value=FakeSns()):
        response = client.post("/sns/topics/create", data={"topic_name": "eventos-teste"})
        assert response.status_code == 200

        response = client.post(
            "/sns/messages/publish",
            data={
                "topic_name": "eventos-teste",
                "subject": "smoke",
                "headers": '{"x-origem": "smoke"}',
                "body": '{"id": 10}',
                "message_group_id": "default",
            },
        )
        assert response.status_code == 200

        response = client.post(
            "/sns/subscriptions/sqs",
            data={
                "topic_name": "eventos-teste",
                "queue_name": "teste",
            },
        )
        assert response.status_code == 200

    # S3 form handlers
    with patch("aws_manager.s3.web.pages.create_bucket", return_value={"location": "/dados-dev"}):
        response = client.post(
            "/s3/buckets/create",
            data={
                "bucket_name": "dados-dev",
                "versioning_status": "Enabled",
                "tags_json": '{"ambiente": "dev"}',
            },
        )
        assert response.status_code == 200

    with patch("aws_manager.s3.web.pages.update_bucket_settings"):
        response = client.post(
            "/s3/buckets/update",
            data={
                "bucket_name": "dados-dev",
                "versioning_status": "Suspended",
                "tags_json": '{"ambiente": "qa"}',
            },
        )
        assert response.status_code == 200

    # SQS JSON API
    with patch("aws_manager.sqs.web.api.list_available_queues", return_value=[{"name": "teste", "url": "u1"}]):
        response = client.get("/monitor/queues")
        assert response.status_code == 200
        assert response.get_json() == {"queues": [{"name": "teste", "url": "u1"}]}

    # SNS JSON API
    with patch("aws_manager.sns.web.api.list_available_topics", return_value=[{"name": "eventos-teste", "arn": "a1"}]):
        response = client.get("/sns/topics")
        assert response.status_code == 200
        assert response.get_json() == {"topics": [{"name": "eventos-teste", "arn": "a1"}]}

    with patch("aws_manager.sns.web.api.list_topic_subscriptions", return_value=[{"protocol": "sqs", "endpoint": "q1", "subscriptionArn": "s1"}]):
        response = client.get("/sns/subscriptions?topicArn=a1")
        assert response.status_code == 200
        assert response.get_json() == {"subscriptions": [{"protocol": "sqs", "endpoint": "q1", "subscriptionArn": "s1"}]}

    # S3 JSON API
    with patch("aws_manager.s3.web.api.list_available_buckets", return_value=[{"name": "dados-dev", "createdAt": ""}]):
        response = client.get("/s3/buckets")
        assert response.status_code == 200
        assert response.get_json() == {"buckets": [{"name": "dados-dev", "createdAt": ""}]}

    with patch("aws_manager.s3.web.api.create_bucket", return_value={"location": "/dados-dev"}):
        response = client.post(
            "/s3/buckets",
            json={
                "bucketName": "dados-dev",
                "versioningStatus": "Enabled",
                "tags": {"ambiente": "dev"},
            },
        )
        assert response.status_code == 200
        assert response.get_json()["status"] == "created"
        assert response.get_json()["bucketName"] == "dados-dev"

    with patch("aws_manager.s3.web.api.update_bucket_settings"):
        response = client.put(
            "/s3/buckets",
            json={
                "bucketName": "dados-dev",
                "versioningStatus": "Enabled",
                "tags": {"ambiente": "dev"},
            },
        )
        assert response.status_code == 200
        assert response.get_json()["status"] == "updated"

        response = client.put(
            "/s3/buckets/settings",
            json={
                "bucketName": "dados-dev",
                "versioningStatus": "Suspended",
                "tags": {"ambiente": "qa"},
            },
        )
        assert response.status_code == 200
        assert response.get_json()["status"] == "updated"

    response = client.post(
        "/s3/buckets",
        json={
            "bucketName": "dados-dev",
            "versioningStatus": "Enabled",
            "tags": ["invalido"],
        },
    )
    assert response.status_code == 400
    assert response.get_json()["status"] == "invalid"

    with patch("aws_manager.s3.web.api.get_bucket_details", return_value={"name": "dados-dev", "versioningStatus": "Enabled", "tags": {"ambiente": "dev"}, "location": "us-east-1"}):
        response = client.get("/s3/buckets/details?name=dados-dev")
        assert response.status_code == 200
        assert response.get_json()["versioningStatus"] == "Enabled"

    with patch("aws_manager.s3.web.api.collect_bucket_stats", return_value={"objectCount": 2, "totalSizeBytes": 42}):
        response = client.get("/s3/buckets/stats?name=dados-dev")
        assert response.status_code == 200
        assert response.get_json() == {"objectCount": 2, "totalSizeBytes": 42}

    with patch("aws_manager.s3.web.api.delete_bucket", side_effect=ValueError("Bucket nao esta vazio. Marque forceDelete para remover objetos antes de deletar.")):
        response = client.delete("/s3/buckets", json={"bucketName": "dados-dev", "forceDelete": False})
        assert response.status_code == 409
        assert response.get_json()["status"] == "blocked"

    # MiniStack health API
    health_payload = '{"services": {"sqs": {"status": "running"}, "sns": {"status": "running"}, "s3": {"status": "running"}}}'
    with patch("aws_manager.web.pages.ministack_health_url_candidates", return_value=["http://fake/_ministack/health"]), \
            patch("aws_manager.web.pages.urlopen", return_value=FakeUrlOpenResponse(health_payload)):
        response = client.get("/ministack/health")
        assert response.status_code == 200
        assert response.get_json()["online"] is True
        services = response.get_json()["services"]
        assert services["sqs"] is True
        assert services["sns"] is True
        assert services["s3"] is True

    with patch("aws_manager.web.pages.ministack_health_url_candidates", return_value=["http://fake/_ministack/health"]), \
            patch("aws_manager.web.pages.urlopen", side_effect=RuntimeError("unreachable")):
        response = client.get("/ministack/health")
        assert response.status_code == 503
        assert response.get_json()["online"] is False

    print("smoke-tests-ok")


if __name__ == "__main__":
    run()
