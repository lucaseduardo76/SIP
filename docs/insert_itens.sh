#!/usr/bin/env bash
# Envia 30 POSTs sequenciais ao endpoint /sip/api/items/admin/create
# Salve como send_30_items.sh e rode: chmod +x send_30_items.sh && ./send_30_items.sh

BASE_URL="http://localhost:8080/sip/api/items/admin/create"
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJTSVAiLCJzdWIiOiIyMDIzMTEyNDAwMTVAaWZiYS5lZHUuYnIiLCJyb2xlIjoiQURNSU4iLCJleHAiOjE3NjI1ODM0MjJ9.ztRNgQGImsfjH9K2d37MXnnERAUTunMEy1NQ4tKPmQA"

send() {
  local payload="$1"
  echo
  echo "----- Enviando payload -----"
  echo "$payload"
  echo "----------------------------"
  curl -sS -X POST "$BASE_URL" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "$payload" \
    -w "\nHTTP_STATUS:%{http_code}\n"
}

# 30 payloads (day_period mantido como "MORNING")
payloads=(
'{"area":"LIBRARY","category":"ELECTRONIC","day_period":"MORNING","description":"Chave de carro vermelha encontrada próximo à biblioteca","finding_date":"2025-08-27"}'
'{"area":"BLOCK_ONE","category":"BOOK","day_period":"MORNING","description":"Livro de matemática perdido na primeira sala do bloco 1","finding_date":"2025-09-01"}'
'{"area":"BLOCK_TWO","category":"CLOTHING","day_period":"MORNING","description":"Jaqueta azul encontrada no corredor do bloco 2","finding_date":"2025-09-03"}'
'{"area":"BLOCK_THREE","category":"ACCESSORY","day_period":"MORNING","description":"Óculos escuros esquecidos na sala do bloco 3","finding_date":"2025-09-05"}'
'{"area":"BLOCK_FOUR","category":"SCHOOL_SUPPLY","day_period":"MORNING","description":"Caderno de física encontrado próximo à porta do bloco 4","finding_date":"2025-09-06"}'
'{"area":"BLOCK_FIVE","category":"DOCUMENT","day_period":"MORNING","description":"Identidade esquecida no corredor do bloco 5","finding_date":"2025-09-07"}'
'{"area":"BLOCK_SIX","category":"CONTAINER","day_period":"MORNING","description":"Garrafa plástica deixada no bebedouro do bloco 6","finding_date":"2025-09-08"}'
'{"area":"BLOCK_SEVEN","category":"ELECTRONIC","day_period":"MORNING","description":"Fone de ouvido encontrado no auditório do bloco 7","finding_date":"2025-09-09"}'
'{"area":"BLOCK_EIGHT","category":"BOTTLE","day_period":"MORNING","description":"Garrafa de água esquecida na escada do bloco 8","finding_date":"2025-09-10"}'
'{"area":"BLOCK_NINE","category":"ACCESSORY","day_period":"MORNING","description":"Chapéu preto encontrado na entrada do bloco 9","finding_date":"2025-09-11"}'
'{"area":"LIBRARY","category":"DOCUMENT","day_period":"MORNING","description":"Carteira de estudante encontrada entre as mesas da biblioteca","finding_date":"2025-09-12"}'
'{"area":"VIDEO_ROOM","category":"ELECTRONIC","day_period":"MORNING","description":"Controle remoto deixado na sala de vídeo","finding_date":"2025-09-13"}'
'{"area":"RC","category":"OTHER","day_period":"MORNING","description":"Chaveiro perdido próximo ao RC","finding_date":"2025-09-14"}'
'{"area":"BLOCK_ONE","category":"CLOTHING","day_period":"MORNING","description":"Boné azul encontrado no corredor do bloco 1","finding_date":"2025-09-15"}'
'{"area":"BLOCK_TWO","category":"BOOK","day_period":"MORNING","description":"Livro de química deixado na sala do bloco 2","finding_date":"2025-09-16"}'
'{"area":"BLOCK_THREE","category":"SCHOOL_SUPPLY","day_period":"MORNING","description":"Estojo de lápis esquecido na escada do bloco 3","finding_date":"2025-09-17"}'
'{"area":"BLOCK_FOUR","category":"ACCESSORY","day_period":"MORNING","description":"Relógio encontrado na sala do bloco 4","finding_date":"2025-09-18"}'
'{"area":"BLOCK_FIVE","category":"CONTAINER","day_period":"MORNING","description":"Garrafa térmica deixada no bloco 5","finding_date":"2025-09-19"}'
'{"area":"BLOCK_SIX","category":"BOTTLE","day_period":"MORNING","description":"Garrafa de refrigerante esquecida no corredor do bloco 6","finding_date":"2025-09-20"}'
'{"area":"BLOCK_SEVEN","category":"ELECTRONIC","day_period":"MORNING","description":"Fone bluetooth encontrado no auditório do bloco 7","finding_date":"2025-09-21"}'
'{"area":"BLOCK_EIGHT","category":"CLOTHING","day_period":"MORNING","description":"Suéter vermelho encontrado na entrada do bloco 8","finding_date":"2025-09-22"}'
'{"area":"BLOCK_NINE","category":"DOCUMENT","day_period":"MORNING","description":"Documento perdido na escada do bloco 9","finding_date":"2025-09-23"}'
'{"area":"LIBRARY","category":"ACCESSORY","day_period":"MORNING","description":"Chaveiro encontrado na biblioteca","finding_date":"2025-09-24"}'
'{"area":"VIDEO_ROOM","category":"BOOK","day_period":"MORNING","description":"Livro de história deixado na sala de vídeo","finding_date":"2025-09-25"}'
'{"area":"RC","category":"SCHOOL_SUPPLY","day_period":"MORNING","description":"Caderno de inglês esquecido próximo ao RC","finding_date":"2025-09-26"}'
'{"area":"BLOCK_ONE","category":"ELECTRONIC","day_period":"MORNING","description":"Carregador de celular encontrado no bloco 1","finding_date":"2025-09-27"}'
'{"area":"BLOCK_TWO","category":"ACCESSORY","day_period":"MORNING","description":"Pulseira esquecida na sala do bloco 2","finding_date":"2025-09-28"}'
'{"area":"BLOCK_THREE","category":"DOCUMENT","day_period":"MORNING","description":"RG encontrado no corredor do bloco 3","finding_date":"2025-09-29"}'
'{"area":"BLOCK_FOUR","category":"OTHER","day_period":"MORNING","description":"Chave aleatória encontrada no bloco 4","finding_date":"2025-09-30"}'
'{"area":"BLOCK_FIVE","category":"CONTAINER","day_period":"MORNING","description":"Pote de plástico esquecido no bloco 5","finding_date":"2025-10-01"}'
'{"area":"BLOCK_SIX","category":"BOOK","day_period":"MORNING","description":"Livro de geografia deixado no bloco 6","finding_date":"2025-10-02"}'
)

# loop and send one-by-one
for p in "${payloads[@]}"; do
  send "$p"
  # opcional: aguarda 0.5s entre requisições para não sobrecarregar o servidor
  sleep 0.5
done

echo
echo "Todos os pedidos foram enviados."