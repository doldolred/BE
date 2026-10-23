#!/usr/bin/env bash
# 새 Lightsail 인스턴스(Ubuntu 24.04) 첫 셋팅 — 한 번만. ubuntu 사용자로: sudo bash setup-server.sh
#   - 스왑 2GB (2GB 인스턴스에서 JVM + Postgres 가 순간적으로 몰려도 죽지 않게)
#   - 시간대 Asia/Seoul
#   - Docker + compose 플러그인 (우분투 저장소 패키지)
#   - 보안 업데이트 자동 설치
set -euo pipefail

if [[ $EUID -ne 0 ]]; then echo "sudo 로 실행하세요"; exit 1; fi
user=${SUDO_USER:-ubuntu}

if ! swapon --show | grep -q /swapfile; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
  sysctl -w vm.swappiness=10
  echo 'vm.swappiness=10' > /etc/sysctl.d/99-swappiness.conf
fi

timedatectl set-timezone Asia/Seoul

apt-get update
DEBIAN_FRONTEND=noninteractive apt-get install -y docker.io docker-compose-v2 git unattended-upgrades
systemctl enable --now docker
usermod -aG docker "$user"

echo
echo "완료. 다시 접속(ssh 재연결)하면 sudo 없이 docker 를 쓸 수 있습니다."
docker --version
docker compose version
