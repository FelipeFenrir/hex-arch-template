from flask import Flask

from .web.pages import home_blueprint
from .sqs.web.pages import sqs_pages_blueprint
from .sqs.web.api import sqs_api_blueprint
from .sns.web.pages import sns_pages_blueprint
from .sns.web.api import sns_api_blueprint
from .s3.web.pages import s3_pages_blueprint
from .s3.web.api import s3_api_blueprint


def create_app() -> Flask:
    app = Flask(__name__, template_folder="../templates", static_folder="../static", static_url_path="/static")
    app.register_blueprint(home_blueprint)
    app.register_blueprint(sqs_pages_blueprint)
    app.register_blueprint(sqs_api_blueprint)
    app.register_blueprint(sns_pages_blueprint)
    app.register_blueprint(sns_api_blueprint)
    app.register_blueprint(s3_pages_blueprint)
    app.register_blueprint(s3_api_blueprint)
    return app

