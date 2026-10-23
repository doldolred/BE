#!/usr/bin/env bash
# 첫 HTTPS 인증서 발급 (서버에서 한 번만). 이후 갱신은 certbot 컨테이너가 알아서 한다.
#
#   전제: DNS A 레코드(DOMAIN, DOMAIN_ALIASES)가 이 서버 고정 IP 를 가리키고, Lightsail 방화벽에 80 · 443 이 열려 있다.
#   사용: cd deploy && ./init-letsencrypt.sh            # 실제 발급
#         STAGING=1 ./init-letsencrypt.sh                # 연습용(발급 횟수 제한 없음, 브라우저는 경고)
set -euo pipefail
cd "$(dirname "$0")"

set -a; source .env; set +a
: "${DOMAIN:?.env 에 DOMAIN 을 넣으세요}"
: "${LETSENCRYPT_EMAIL:?.env 에 LETSENCRYPT_EMAIL 을 넣으세요}"

domains=(-d "$DOMAIN")
for alias in ${DOMAIN_ALIASES:-}; do
  [[ "$alias" == *.invalid ]] || domains+=(-d "$alias")
done
extra=()
[[ "${STAGING:-0}" == "1" ]] && extra+=(--staging)
[[ "${FORCE:-0}" == "1" ]] && extra+=(--force-renewal)

mkdir -p certbot/www certbot/conf

if [[ -e "certbot/conf/live/$DOMAIN/fullchain.pem" && "${FORCE:-0}" != "1" ]]; then
  echo "이미 인증서가 있습니다: certbot/conf/live/$DOMAIN  (다시 받으려면 FORCE=1)"
  exit 0
fi

# 80 포트를 certbot 이 잠깐 직접 쓴다 — edge(nginx)가 떠 있으면 내린다
docker compose -f docker-compose.prod.yml stop edge 2>/dev/null || true

docker compose -f docker-compose.prod.yml run --rm -p 80:80 --entrypoint certbot certbot \
  certonly --standalone "${extra[@]}" \
  "${domains[@]}" \
  --email "$LETSENCRYPT_EMAIL" --agree-tos --no-eff-email --non-interactive

echo "발급 완료 → edge 시작"
docker compose -f docker-compose.prod.yml up -d edge
