FROM node:20-alpine AS ui-build

WORKDIR /app/ui

COPY ui/package.json ./
RUN npm ci

COPY ui/ ./
RUN npm run build


FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY aws_manager ./aws_manager
COPY --from=ui-build /app/ui/dist ./ui/dist

CMD ["python", "-m", "flask", "--app", "aws_manager", "run", "--host", "0.0.0.0", "--port", "5000"]
