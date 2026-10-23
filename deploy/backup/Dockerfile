# 매일 DB 덤프 — pg_dump 버전을 DB(postgres:17)와 맞추려고 같은 이미지에서 시작
FROM postgres:17-alpine
RUN apk add --no-cache aws-cli
COPY backup.sh backup-loop.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/backup.sh /usr/local/bin/backup-loop.sh
ENTRYPOINT []
CMD ["backup-loop.sh"]
