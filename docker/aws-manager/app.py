from aws_manager import create_app
from aws_manager.config import is_debug_enabled

app = create_app()


if __name__ == "__main__":
    debug_enabled = is_debug_enabled()
    app.run(host="0.0.0.0", port=5000, debug=debug_enabled, use_reloader=debug_enabled)
