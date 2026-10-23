#!/bin/sh
# 매일 BACKUP_AT_KST_HOUR 시(한국 시간)에 backup.sh 실행. cron 대신 단순 반복 — 컨테이너 환경변수가 그대로 전달된다.
set -eu
hour_kst=${BACKUP_AT_KST_HOUR:-4}
hour_utc=$(( (hour_kst + 24 - 9) % 24 ))

echo "[backup] 매일 ${hour_kst}시(KST)에 백업, ${BACKUP_KEEP_DAYS:-14}일 보관"
while :; do
  now=$(date -u +%s)
  wait=$(( (hour_utc * 3600 - now % 86400 + 86400) % 86400 ))
  [ "$wait" -eq 0 ] && wait=86400
  sleep "$wait"
  backup.sh || echo "[backup] 실패 — 다음 회차에 다시 시도합니다" >&2
done
