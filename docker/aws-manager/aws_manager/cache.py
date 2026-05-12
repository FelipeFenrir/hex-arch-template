import threading
import time


class TTLCache:
    def __init__(self):
        self._store: dict[str, tuple[float, object]] = {}
        self._lock = threading.Lock()

    def get(self, key: str):
        now = time.time()
        with self._lock:
            entry = self._store.get(key)
            if not entry:
                return None
            expires_at, value = entry
            if expires_at <= now:
                self._store.pop(key, None)
                return None
            return value

    def set(self, key: str, value, ttl_seconds: int) -> None:
        with self._lock:
            self._store[key] = (time.time() + max(ttl_seconds, 1), value)

    def delete(self, key: str) -> None:
        with self._lock:
            self._store.pop(key, None)


cache = TTLCache()

