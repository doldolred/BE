#!/bin/sh
# DB 덤프 한 번. 지금 바로: docker compose -f docker-compose.prod.yml exec backup backup.sh
# 접속 정보는 PGHOST · PGDATABASE · PGUSER · PGPASSWORD 환경변수.
set -eu
keep=${BACKUP_KEEP_DAYS:-14}
name="doldol-$(TZ=UTC-9 date +%Y%m%d-%H%M%S).dump"   # 파일 이름은 한국 시간
file="/backups/$name"

# -Fc: 압축된 사용자 지정 형식 — pg_restore 로 표 하나만 되살리는 것도 된다
pg_dump -Fc --no-owner -f "$file.tmp"
mv "$file.tmp" "$file"
echo "[backup] $(date -u +%FT%TZ) 저장 $file ($(du -h "$file" | cut -f1))"

# 상품 이미지 (있을 때만)
uploads=""
if [ -d /uploads ] && [ -n "$(ls -A /uploads 2>/dev/null)" ]; then
  uploads="/backups/${name%.dump}-uploads.tar.gz"
  tar -czf "$uploads.tmp" -C /uploads . && mv "$uploads.tmp" "$uploads"
  echo "[backup] 이미지 $uploads ($(du -h "$uploads" | cut -f1))"
fi

find /backups -name 'doldol-*' -mtime +"$keep" -print -delete

if [ -n "${BACKUP_S3_URI:-}" ]; then
  aws s3 cp --only-show-errors "$file" "${BACKUP_S3_URI%/}/$name"
  [ -n "$uploads" ] && aws s3 cp --only-show-errors "$uploads" "${BACKUP_S3_URI%/}/$(basename "$uploads")"
  echo "[backup] 업로드 ${BACKUP_S3_URI%/}/$name"
fi
