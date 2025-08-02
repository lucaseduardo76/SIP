#!/bin/sh

echo "Aguardando o MinIO iniciar..."

count=0
until mc alias set local http://minio:9000 admin admin123 >/dev/null 2>&1; do
  sleep 2
  count=$((count + 1))
  if [ $count -ge 30 ]; then
    echo "⛔ Timeout esperando o MinIO"
    exit 1
  fi
done

echo "✅ MinIO está online!"

mc mb --ignore-existing local/profimage
mc anonymous set download local/profimage

echo "✅ Bucket criado e tornado público com sucesso!"