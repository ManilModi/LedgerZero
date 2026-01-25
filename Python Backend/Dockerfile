# ✅ Use Python 3.11 Slim (Smaller, Secure)
FROM python:3.11-slim-bookworm

# 1. Set Environment Variables
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=off \
    PIP_DISABLE_PIP_VERSION_CHECK=on

WORKDIR /app

# 2. Install System Dependencies
# libgomp1 is strictly needed for PyTorch/ONNX
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libpq-dev \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

# 3. Create a non-root user
RUN addgroup --system appgroup && adduser --system --group appuser

# 4. Install Python Dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copy Application Code AND set permissions in one step (Optimized)
# This saves a docker layer and build time
COPY --chown=appuser:appgroup . .

# 6. Switch to non-root user
USER appuser

# 7. Expose the port
EXPOSE 8000

# 8. Run FastAPI
CMD ["python", "-m", "uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]