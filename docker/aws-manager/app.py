"""Entry point for running the AWS Manager Flask app locally.

Usage:
    python app.py
    flask --app app run --debug --port 5000
"""

from aws_manager import create_app

app = create_app()

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)

