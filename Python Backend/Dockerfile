# Use Python 3.9 Slim
FROM python:3.9-slim

WORKDIR /app

# Install System Dependencies for Postgres (psycopg2)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# Install Python Libraries
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy App Code
COPY app.py .

# Run FastAPI
# 0.0.0.0 allows access from outside container
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]