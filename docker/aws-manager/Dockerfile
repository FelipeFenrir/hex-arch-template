FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY app.py .
COPY aws_manager ./aws_manager
COPY templates ./templates
COPY static ./static
COPY data ./data

CMD ["python", "app.py"]