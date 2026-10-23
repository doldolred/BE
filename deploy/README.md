# 운영 배포 — AWS Lightsail 1대

```
인터넷 ─▶ edge(nginx · 80/443 · HTTPS) ─┬─▶ frontend(정적 파일)
                                         └─▶ backend(/api) ─▶ db(Postgres)
certbot : 인증서 자동 갱신        backup : 매일 DB 덤프 (+ Lightsail 버킷)
```

| 파일 | 하는 일 |
| --- | --- |
| `docker-compose.prod.yml` | 운영 구성. 밖으로 열리는 건 edge 의 80 · 443 뿐 |
| `nginx/templates/default.conf.template` | HTTPS · http→https · doldol.co.kr→www · 로그인 요청 제한(IP당 초당 5회) · `/actuator` 비공개 |
| `.env.prod.example` | 운영 값 견본 → 서버에서 `.env` 로 복사 |
| `setup-server.sh` | 새 서버 첫 셋팅 (스왑 2GB · 시간대 · Docker) |
| `init-letsencrypt.sh` | 첫 HTTPS 인증서 발급 (이후 갱신은 자동) |
| `deploy.sh` | 새 버전 배포 (이미지 받기 → 다시 띄우기 → API 확인) |
| `backup/` · `restore.sh` | 매일 DB 백업 · 복원 |
| `../.github/workflows/docker-image.yml` | main push → 테스트 → API 이미지를 GHCR 에 (FE 저장소에도 같은 이름으로 있음) |

## 1. Lightsail 준비 (콘솔)

1. **인스턴스 만들기** — 서울(ap-northeast-2) · Linux/Unix · OS 만 · **Ubuntu 24.04** · **2GB RAM 요금제**
   (JVM + Postgres + nginx 가 1.2GB 안팎을 쓴다. 이미지는 CI 가 만들고 서버는 받기만 하므로 2GB 로 충분)
2. **고정 IP** 만들어서 인스턴스에 붙이기 (붙어 있는 동안은 무료)
3. **네트워킹 → IPv4 방화벽**: SSH 22 · HTTP 80 · HTTPS 443 만. 5432 · 8080 은 열지 않는다
4. **스냅샷 → 자동 스냅샷 켜기** (서버 통째 백업. DB 덤프와 별개로)
5. (선택) **스토리지 → 버킷** 만들기 → 액세스 키 발급 → `.env` 의 `BACKUP_S3_*` 에 넣으면 DB 덤프가 버킷에도 올라간다
6. **도메인 DNS** — `www.doldol.co.kr` 와 `doldol.co.kr` 둘 다 A 레코드 → 고정 IP
   (도메인 산 곳의 DNS 든 Lightsail DNS 영역이든 상관없음)

## 2. 서버 첫 셋팅 (ssh 로 접속해서)

```bash
git clone https://github.com/doldolred/BE.git ~/doldol/BE
sudo bash ~/doldol/BE/deploy/setup-server.sh
exit        # 다시 접속해야 docker 를 sudo 없이 쓸 수 있다
```

GHCR 이미지가 비공개라면 읽기 권한 토큰으로 로그인 (GitHub → Settings → Developer settings → Personal access tokens, `read:packages`):

```bash
echo <토큰> | docker login ghcr.io -u <GitHub 아이디> --password-stdin
```

## 3. 값 넣고 띄우기

```bash
cd ~/doldol/BE/deploy
cp .env.prod.example .env
vi .env      # DOMAIN · LETSENCRYPT_EMAIL · DB_PASSWORD · JWT_SECRET · NICEPAY_* 등
             # 무작위 값: openssl rand -base64 48

bash init-letsencrypt.sh     # 첫 인증서 (DNS 가 이 서버를 가리킨 뒤에). 연습은 STAGING=1 bash init-letsencrypt.sh
bash deploy.sh               # 이미지 받기 → 전부 띄우기 → https://DOMAIN/api/products 확인
```

> 이미지는 각 저장소에 main 이 한 번 push 돼 CI 가 돌아야 생긴다 (GitHub → Actions 탭).
> 급할 때는 서버에서 직접 빌드: FE 를 `~/doldol/FE` 에 clone 한 뒤 `BUILD=1 bash deploy.sh` (빌드 중엔 메모리를 많이 쓴다)

## 4. 평소 운영

| 하고 싶은 것 | 명령 (`~/doldol/BE/deploy` 에서) |
| --- | --- |
| 새 버전 배포 | `bash deploy.sh` |
| 상태 | `docker compose -f docker-compose.prod.yml ps` |
| 로그 | `docker compose -f docker-compose.prod.yml logs -f --tail=100 backend` |
| 지금 바로 DB 백업 | `docker compose -f docker-compose.prod.yml exec backup backup.sh` |
| 백업 목록 | `ls -lh backups/` (매일 04시, 14일 보관) |
| 복원 | `bash restore.sh backups/doldol-YYYYMMDD-HHMMSS.dump` (복원 직전 상태를 먼저 백업함) |
| 메모리 사용량 | `docker stats --no-stream` |

로그는 컨테이너마다 10MB × 5개까지만 남는다(디스크 가득 참 방지).

## 5. 오픈 전 체크

- [ ] 나이스페이 **운영 키**로 교체: `.env` 의 `NICEPAY_CLIENT_ID` · `NICEPAY_SECRET_KEY`, `NICEPAY_API_URL=https://api.nicepay.co.kr`
  + FE 저장소 Actions **Variables** 의 `NICEPAY_CLIENT_ID` 도 바꾸고 FE 다시 빌드(공개 키는 빌드 때 박힌다)
- [ ] 실제 카드로 소액 결제 → 주문 상세 확인 → 취소(환불)까지 한 번
- [ ] HTTPS 가 며칠 문제없으면 `nginx/templates/default.conf.template` 의 `Strict-Transport-Security` 주석 해제 → `bash deploy.sh`
- [ ] 백업 복원 연습 한 번 (다른 날짜 덤프로 `restore.sh`)
- [ ] 관리자 계정: 가입 후 DB 에서 한 번만 `update member set role='ADMIN' where email='...'`
  (`docker compose -f docker-compose.prod.yml exec db psql -U doldol -d doldol`)

## 나중에 바꿀 때

- **트래픽이 늘면**: 스냅샷 → 더 큰 요금제로 새 인스턴스 → 고정 IP 옮기기 (몇 분 중단)
- **DB 를 따로**: Lightsail 관리형 DB 로 옮기고 `db` 서비스를 빼면 된다 (`DB_URL` 만 변경)
- **k8s**: `../k8s/` 매니페스트는 그대로 있다. 지금 규모(일 결제 수십 건 이하)에선 compose 로 충분
