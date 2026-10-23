#!/usr/bin/env bash
# 백업 덤프로 DB 되살리기 — 지금 DB 내용을 덮어쓴다.
#   사용: cd deploy && ./restore.sh backups/doldol-20261101-040000.dump
#   (버킷에 있는 파일이면 먼저 받아 와서 backups/ 에 둔다)
set -euo pipefail
cd "$(dirname "$0")"
file=${1:?복원할 덤프 파일을 주세요. 예: ./restore.sh backups/doldol-20261101-040000.dump}
[[ -f "$file" ]] || { echo "파일이 없습니다: $file"; exit 1; }

set -a; source .env; set +a
compose=(docker compose -f docker-compose.prod.yml)

read -r -p "지금 DB(${DB_NAME:-doldol})를 $file 내용으로 덮어씁니다. 계속할까요? [yes/no] " answer
[[ "$answer" == "yes" ]] || { echo "취소했습니다"; exit 1; }

# 복원 직전 상태도 남겨 둔다
"${compose[@]}" exec -T backup backup.sh

"${compose[@]}" stop backend
"${compose[@]}" exec -T db pg_restore --clean --if-exists --no-owner \
  -U "${DB_USERNAME:-doldol}" -d "${DB_NAME:-doldol}" < "$file"
"${compose[@]}" start backend
echo "복원 완료"
