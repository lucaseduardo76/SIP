echo "Aguardando o MinIO iniciar..."

count=0
until mc alias ls local >/dev/null 2>&1; do
  sleep 2
  count=$((count + 1))
  if [ $count -ge 30 ]; then
    echo "Timeout esperando MinIO"
    exit 1
  fi
done

echo "MinIO está online!"

mc alias set local http://minio:9000 admin admin123

 Bucket profimage
mc mb --ignore-existing local/profimage
sleep 1
mc anonymous set download local/profimage

# Bucket itemsimage
mc mb --ignore-existing local/itemsimage
sleep 1
mc anonymous set download local/itemsimage

echo "Buckets criados e tornados públicos com sucesso!"