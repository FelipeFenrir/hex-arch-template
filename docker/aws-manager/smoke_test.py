from unittest.mock import patch

from aws_manager import create_app

_app = create_app()


class FakeSqs:
    def create_queue(self, QueueName, Attributes=None):
        return {"QueueUrl": f"http://ministack:4566/000000000000/{QueueName}"}

    def get_queue_url(self, QueueName):
        return {"QueueUrl": f"http://ministack:4566/000000000000/{QueueName}"}

    def send_message_batch(self, QueueUrl, Entries):
        assert QueueUrl.endswith("teste")
        assert len(Entries) == 2
        return {"Successful": [{"Id": entry["Id"]} for entry in Entries]}

    def send_message(self, QueueUrl, MessageBody, MessageAttributes=None):
        assert QueueUrl.endswith("teste")
        assert MessageBody
        return {"MessageId": "msg-1"}


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


class FakeGenericClient:
    def __init__(self, service_name):
        self.service_name = service_name

    def list_queues(self):
        return {"QueueUrls": ["http://ministack:4566/000000000000/teste"]}

    def list_topics(self):
        return {"Topics": [{"TopicArn": "arn:aws:sns:us-east-1:000000000000:eventos-teste"}]}

    def list_buckets(self):
        return {"Buckets": [{"Name": "dados-dev"}]}

    def list_tables(self):
        return {"TableNames": ["orders"]}

    def list_functions(self):
        return {"Functions": [{"FunctionName": "fn-1"}]}

    def describe_table(self, TableName):
        return {"Table": {"TableName": TableName, "ItemCount": 1}}

    def get_topic_attributes(self, TopicArn):
        return {"Attributes": {"TopicArn": TopicArn}}

    def get_queue_attributes(self, QueueUrl, AttributeNames=None):
        return {"Attributes": {"QueueArn": "arn:aws:sqs:us-east-1:000000000000:teste"}}

    def list_objects_v2(self, Bucket):
        return {"Contents": [{"Key": "a.txt"}]}


def run():
    client = _app.test_client()

    # SPA root (works even when ui/dist is not built)
    response = client.get("/")
    assert response.status_code == 200

    # Core API health
    response = client.get("/api/health")
    assert response.status_code == 200
    assert response.get_json()["status"] == "ok"

    response = client.get("/api/endpoints")
    assert response.status_code == 200
    assert isinstance(response.get_json().get("routes"), list)

    # Registry-driven stats/resources
    with patch("aws_manager.web.api._build_client", side_effect=lambda service, endpoint=None: FakeGenericClient(service)):
        stats = client.get("/api/stats")
        assert stats.status_code == 200
        stats_json = stats.get_json()
        assert "services" in stats_json
        assert "s3" in stats_json["services"]
        assert "sqs" in stats_json["services"]
        assert "sns" in stats_json["services"]

        resources = client.get("/api/resources/dynamodb")
        assert resources.status_code == 200
        assert "tables" in resources.get_json()["resources"]

        detail = client.get("/api/resources/dynamodb/tables/orders")
        assert detail.status_code == 200
        assert detail.get_json()["id"] == "orders"

    # SQS JSON APIs
    with patch("aws_manager.sqs.web.api.list_available_queues", return_value=[{"name": "teste", "url": "u1"}]):
        response = client.get("/api/sqs/queues")
        assert response.status_code == 200
        assert response.get_json() == {"queues": [{"name": "teste", "url": "u1"}]}

    with patch("aws_manager.sqs.web.api.build_sqs_client", return_value=FakeSqs()):
        response = client.post("/api/sqs/queues", json={"queueName": "teste"})
        assert response.status_code == 200
        assert response.get_json()["status"] == "created"

        response = client.post("/api/sqs/queues/teste/messages", json={"body": {"id": 1}, "headers": {}})
        assert response.status_code == 200
        assert response.get_json()["status"] == "sent"

    # SNS JSON APIs
    with patch("aws_manager.sns.web.api.list_available_topics", return_value=[{"name": "eventos-teste", "arn": "a1"}]):
        response = client.get("/api/sns/topics")
        assert response.status_code == 200
        assert response.get_json() == {"topics": [{"name": "eventos-teste", "arn": "a1"}]}

    with patch("aws_manager.sns.web.api.list_topic_subscriptions", return_value=[{"protocol": "sqs", "endpoint": "q1", "subscriptionArn": "s1"}]):
        response = client.get("/sns/subscriptions?topicArn=a1")
        assert response.status_code == 200
        assert response.get_json() == {"subscriptions": [{"protocol": "sqs", "endpoint": "q1", "subscriptionArn": "s1"}]}

    with patch("aws_manager.sns.web.api.build_sns_client", return_value=FakeSns()):
        response = client.post("/api/sns/topics", json={"topicName": "eventos-teste"})
        assert response.status_code == 200
        assert response.get_json()["status"] == "created"

    # S3 JSON APIs
    with patch("aws_manager.s3.web.api.list_available_buckets", return_value=[{"name": "dados-dev", "createdAt": ""}]):
        response = client.get("/api/s3/buckets")
        assert response.status_code == 200
        assert response.get_json() == {"buckets": [{"name": "dados-dev", "createdAt": ""}]}

    with patch("aws_manager.s3.web.api.create_bucket", return_value={"location": "/dados-dev"}):
        response = client.post(
            "/api/s3/buckets",
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
            "/api/s3/buckets",
            json={
                "bucketName": "dados-dev",
                "versioningStatus": "Enabled",
                "tags": {"ambiente": "dev"},
            },
        )
        assert response.status_code == 200
        assert response.get_json()["status"] == "updated"

        response = client.put(
            "/api/s3/buckets/settings",
            json={
                "bucketName": "dados-dev",
                "versioningStatus": "Suspended",
                "tags": {"ambiente": "qa"},
            },
        )
        assert response.status_code == 200
        assert response.get_json()["status"] == "updated"

    response = client.post(
        "/api/s3/buckets",
        json={
            "bucketName": "dados-dev",
            "versioningStatus": "Enabled",
            "tags": ["invalido"],
        },
    )
    assert response.status_code == 400
    assert response.get_json()["status"] == "invalid"

    with patch("aws_manager.s3.web.api.get_bucket_details", return_value={"name": "dados-dev", "versioningStatus": "Enabled", "tags": {"ambiente": "dev"}, "location": "us-east-1"}):
        response = client.get("/api/s3/buckets/details?name=dados-dev")
        assert response.status_code == 200
        assert response.get_json()["versioningStatus"] == "Enabled"

    with patch("aws_manager.s3.web.api.collect_bucket_stats", return_value={"objectCount": 2, "totalSizeBytes": 42}):
        response = client.get("/api/s3/buckets/stats?name=dados-dev")
        assert response.status_code == 200
        assert response.get_json() == {"objectCount": 2, "totalSizeBytes": 42}

    with patch("aws_manager.s3.web.api.delete_bucket", side_effect=ValueError("Bucket nao esta vazio. Marque forceDelete para remover objetos antes de deletar.")):
        response = client.delete("/api/s3/buckets", json={"bucketName": "dados-dev", "forceDelete": False})
        assert response.status_code == 409
        assert response.get_json()["status"] == "blocked"

    # MiniStack health API
    health_payload = '{"services": {"sqs": {"status": "running"}, "sns": {"status": "running"}, "s3": {"status": "running"}}}'
    with patch("aws_manager.web.api.ministack_health_url_candidates", return_value=["http://fake/_ministack/health"]), \
            patch("aws_manager.web.api.urlopen", return_value=FakeUrlOpenResponse(health_payload)):
        response = client.get("/api/ministack/health")
        assert response.status_code == 200
        assert response.get_json()["online"] is True
        services = response.get_json()["services"]
        assert services["sqs"] is True
        assert services["sns"] is True
        assert services["s3"] is True

    with patch("aws_manager.web.api.ministack_health_url_candidates", return_value=["http://fake/_ministack/health"]), \
            patch("aws_manager.web.api.urlopen", side_effect=RuntimeError("unreachable")):
        response = client.get("/api/ministack/health")
        assert response.status_code == 503
        assert response.get_json()["online"] is False

    print("smoke-tests-ok")


if __name__ == "__main__":
    run()
