#!/bin/sh
set -e

echo "Configuring MinIO client alias..."

# Configure alias first (this does *not* require MinIO to be up)
mc alias set local http://minio:9000 admin admin123

echo "Waiting for MinIO to start..."

count=0
until mc ls local >/dev/null 2>&1; do
  sleep 2
  count=$((count + 1))
  if [ "$count" -ge 30 ]; then
    echo "Timeout waiting for MinIO"
    exit 1
  fi
done

echo "MinIO is online!"

# Bucket profimage
mc mb --ignore-existing local/profimage
sleep 1
mc anonymous set download local/profimage

# Bucket itemsimage
mc mb --ignore-existing local/itemsimage
sleep 1
mc anonymous set download local/itemsimage

echo "Buckets created and made public successfully!"