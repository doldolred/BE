#!/usr/bin/env bash
# 새 버전 배포 — CI 가 GHCR 에 올린 최신 이미지를 받아 다시 띄운다.
#   cd deploy && ./deploy.sh            # 이미지 받아서 배포
#   BUILD=1 ./deploy.sh                  # 서버에서 직접 빌드 (BE · FE 저장소가 나란히 clone 돼 있을 때)
set -euo pipefail
cd "$(dirname "$0")"
compose=(docker compose -f docker-compose.prod.yml)

git -C .. pull --ff-only   # 배포 설정(compose · nginx) 최신으로

if [[ "${BUILD:-0}" == "1" ]]; then
  git -C ../../FE pull --ff-only
  "${compose[@]}" build backend frontend
else
  "${compose[@]}" pull backend frontend
fi
"${compose[@]}" build backup
"${compose[@]}" up -d
docker image prune -f >/dev/null

# 백엔드가 뜰 때까지 (DB 마이그레이션 포함) 기다렸다가 확인
for _ in $(seq 1 30); do
  if curl -fsS -o /dev/null "https://$(grep '^DOMAIN=' .env | cut -d= -f2)/api/products"; then
    echo "배포 완료 — 상품 API 응답 OK"
    "${compose[@]}" ps
    exit 0
  fi
  sleep 3
done
echo "API 가 응답하지 않습니다. 로그: docker compose -f docker-compose.prod.yml logs --tail=100 backend" >&2
exit 1
