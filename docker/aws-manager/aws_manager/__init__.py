from pathlib import Path

from flask import Flask, abort, send_from_directory

from .web.api import api_blueprint
from .sqs.web.api import sqs_api_blueprint
from .sns.web.api import sns_api_blueprint
from .s3.web.api import s3_api_blueprint
from .dynamodb.web.api import dynamodb_api_blueprint


def create_app() -> Flask:
    app = Flask(__name__)
    app.register_blueprint(api_blueprint)
    app.register_blueprint(sqs_api_blueprint)
    app.register_blueprint(sns_api_blueprint)
    app.register_blueprint(s3_api_blueprint)
    app.register_blueprint(dynamodb_api_blueprint)

    dist_dir = Path(__file__).resolve().parent.parent / "ui" / "dist"

    @app.get("/")
    def spa_root():
        if dist_dir.exists():
            return send_from_directory(dist_dir, "index.html")
        return {
            "status": "ui-not-built",
            "message": "UI dist not found. Build frontend in docker/aws-manager/ui.",
        }

    @app.get("/<path:path>")
    def spa_assets(path: str):
        if path.startswith("api/") or path.startswith("s3/") or path.startswith("sns/") or path.startswith("monitor/") or path.startswith("dynamodb/"):
            return abort(404)

        if not dist_dir.exists():
            return abort(404)

        candidate = dist_dir / path
        if candidate.exists() and candidate.is_file():
            return send_from_directory(dist_dir, path)
        return send_from_directory(dist_dir, "index.html")

    return app

