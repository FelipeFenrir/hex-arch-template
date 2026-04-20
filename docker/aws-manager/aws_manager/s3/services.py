import re
from datetime import datetime

from .clients import build_s3_client

BUCKET_NAME_PATTERN = re.compile(r"^(?!\d+\.\d+\.\d+\.\d+$)(?!.*\.\.)[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$")


def validate_bucket_name(bucket_name: str) -> str | None:
    """Return a validation message when bucket name is invalid."""
    if not bucket_name:
        return "Informe o nome do bucket."

    if len(bucket_name) < 3 or len(bucket_name) > 63:
        return "Nome do bucket deve conter entre 3 e 63 caracteres."

    if not BUCKET_NAME_PATTERN.match(bucket_name):
        return "Nome do bucket invalido. Use apenas letras minusculas, numeros, ponto e hifen."

    return None


def normalize_tags(tags: dict | None) -> list[dict[str, str]]:
    if not tags:
        return []

    normalized: list[dict[str, str]] = []
    for key, value in tags.items():
        normalized.append({"Key": str(key), "Value": str(value)})

    return normalized


def list_available_buckets() -> list[dict[str, str]]:
    s3 = build_s3_client()
    response = s3.list_buckets()

    buckets: list[dict[str, str]] = []
    for bucket in response.get("Buckets", []):
        created = bucket.get("CreationDate")
        created_iso = created.isoformat() if isinstance(created, datetime) else ""
        buckets.append({"name": bucket.get("Name", "-"), "createdAt": created_iso})

    return sorted(buckets, key=lambda item: item["name"].lower())


def get_bucket_versioning_status(bucket_name: str) -> str:
    s3 = build_s3_client()
    response = s3.get_bucket_versioning(Bucket=bucket_name)
    return response.get("Status", "Disabled")


def get_bucket_tags(bucket_name: str) -> dict[str, str]:
    s3 = build_s3_client()

    try:
        response = s3.get_bucket_tagging(Bucket=bucket_name)
    except Exception as exc:
        error_code = getattr(exc, "response", {}).get("Error", {}).get("Code")
        if error_code in {"NoSuchTagSet", "NoSuchBucket"}:
            return {}
        raise

    result: dict[str, str] = {}
    for tag in response.get("TagSet", []):
        key = str(tag.get("Key", "")).strip()
        if key:
            result[key] = str(tag.get("Value", ""))

    return result


def get_bucket_location(bucket_name: str) -> str:
    s3 = build_s3_client()
    response = s3.get_bucket_location(Bucket=bucket_name)
    return response.get("LocationConstraint") or "us-east-1"


def collect_bucket_stats(bucket_name: str) -> dict[str, int]:
    s3 = build_s3_client()
    paginator = s3.get_paginator("list_objects_v2")

    object_count = 0
    total_size_bytes = 0

    for page in paginator.paginate(Bucket=bucket_name):
        contents = page.get("Contents", [])
        object_count += len(contents)
        total_size_bytes += sum(int(item.get("Size", 0)) for item in contents)

    return {"objectCount": object_count, "totalSizeBytes": total_size_bytes}


def bucket_has_any_content(bucket_name: str) -> bool:
    """Return True when the bucket has objects or versioned entries."""
    s3 = build_s3_client()

    objects_page = s3.list_objects_v2(Bucket=bucket_name, MaxKeys=1)
    if objects_page.get("KeyCount", 0) > 0:
        return True

    try:
        versions_page = s3.list_object_versions(Bucket=bucket_name, MaxKeys=1)
        if versions_page.get("Versions") or versions_page.get("DeleteMarkers"):
            return True
    except Exception:
        # Emulator may not implement version listing for non-versioned buckets.
        pass

    return False


def purge_bucket_contents(bucket_name: str) -> None:
    """Delete all objects and versioned entries from a bucket."""
    s3 = build_s3_client()

    paginator = s3.get_paginator("list_objects_v2")
    for page in paginator.paginate(Bucket=bucket_name):
        keys = [{"Key": item["Key"]} for item in page.get("Contents", []) if item.get("Key")]
        if keys:
            s3.delete_objects(Bucket=bucket_name, Delete={"Objects": keys})

    try:
        versions_paginator = s3.get_paginator("list_object_versions")
        for page in versions_paginator.paginate(Bucket=bucket_name):
            versioned = [
                {"Key": item["Key"], "VersionId": item["VersionId"]}
                for item in page.get("Versions", [])
                if item.get("Key") and item.get("VersionId")
            ]
            delete_markers = [
                {"Key": item["Key"], "VersionId": item["VersionId"]}
                for item in page.get("DeleteMarkers", [])
                if item.get("Key") and item.get("VersionId")
            ]
            entries = versioned + delete_markers
            if entries:
                s3.delete_objects(Bucket=bucket_name, Delete={"Objects": entries})
    except Exception:
        # Ignore when emulator does not support version APIs.
        pass


def get_bucket_details(bucket_name: str) -> dict:
    return {
        "name": bucket_name,
        "versioningStatus": get_bucket_versioning_status(bucket_name),
        "tags": get_bucket_tags(bucket_name),
        "location": get_bucket_location(bucket_name),
    }


def create_bucket(bucket_name: str, versioning_status: str = "Disabled", tags: dict | None = None) -> dict:
    s3 = build_s3_client()
    response = s3.create_bucket(Bucket=bucket_name)

    if versioning_status in {"Enabled", "Suspended"}:
        s3.put_bucket_versioning(Bucket=bucket_name, VersioningConfiguration={"Status": versioning_status})

    normalized_tags = normalize_tags(tags)
    if normalized_tags:
        s3.put_bucket_tagging(Bucket=bucket_name, Tagging={"TagSet": normalized_tags})

    return {"location": response.get("Location", "-")}


def update_bucket_settings(bucket_name: str, versioning_status: str, tags: dict | None = None) -> None:
    s3 = build_s3_client()

    target_status = versioning_status
    if versioning_status == "Disabled":
        # In S3, the closest runtime equivalent to disabling after enablement is suspension.
        target_status = "Suspended"

    if target_status in {"Enabled", "Suspended"}:
        s3.put_bucket_versioning(Bucket=bucket_name, VersioningConfiguration={"Status": target_status})

    normalized_tags = normalize_tags(tags)
    if normalized_tags:
        s3.put_bucket_tagging(Bucket=bucket_name, Tagging={"TagSet": normalized_tags})
    else:
        try:
            s3.delete_bucket_tagging(Bucket=bucket_name)
        except Exception:
            # Some emulators do not support delete_bucket_tagging for empty tag sets.
            pass


def delete_bucket(bucket_name: str, force_delete: bool = False) -> None:
    if bucket_has_any_content(bucket_name) and not force_delete:
        raise ValueError("Bucket nao esta vazio. Marque forceDelete para remover objetos antes de deletar.")

    s3 = build_s3_client()

    if force_delete:
        purge_bucket_contents(bucket_name)

    s3.delete_bucket(Bucket=bucket_name)


